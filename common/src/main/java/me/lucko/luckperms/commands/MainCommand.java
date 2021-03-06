/*
 * Copyright (c) 2016 Lucko (Luck) <luck@lucko.me>
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included in all
 *  copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 *  SOFTWARE.
 */

package me.lucko.luckperms.commands;

import com.google.common.collect.ImmutableList;
import lombok.AllArgsConstructor;
import lombok.Getter;
import me.lucko.luckperms.LuckPermsPlugin;
import me.lucko.luckperms.constants.Message;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Getter
@AllArgsConstructor
public abstract class MainCommand<T> {

    /**
     * The name of the main command
     */
    private final String name;

    /**
     * The command usage
     */
    private final String usage;

    /**
     * How many arguments are required for the command to run
     */
    private final int requiredArgsLength;

    /**
     * A list of the sub commands under this main command
     */
    @Getter
    private final List<SubCommand<T>> subCommands;

    MainCommand(String name, String usage, int requiredArgsLength) {
        this(name, usage, requiredArgsLength, ImmutableList.of());
    }

    /**
     * Called when this main command is ran
     * @param plugin a link to the main plugin instance
     * @param sender the sender to executed the command
     * @param args the stripped arguments given
     * @param label the command label used
     */
    protected CommandResult execute(LuckPermsPlugin plugin, Sender sender, List<String> args, String label) {
        if (args.size() < requiredArgsLength) {
            sendUsage(sender, label);
            return CommandResult.INVALID_ARGS;
        }

        Optional<SubCommand<T>> o = getSubCommands().stream()
                .filter(s -> s.getName().equalsIgnoreCase(args.get(requiredArgsLength - 1)))
                .limit(1)
                .findAny();

        if (!o.isPresent()) {
            Message.COMMAND_NOT_RECOGNISED.send(sender);
            return CommandResult.INVALID_ARGS;
        }

        final SubCommand<T> sub = o.get();
        if (!sub.isAuthorized(sender)) {
            Message.COMMAND_NO_PERMISSION.send(sender);
            return CommandResult.NO_PERMISSION;
        }

        List<String> strippedArgs = new ArrayList<>();
        if (args.size() > requiredArgsLength) {
            strippedArgs.addAll(args.subList(requiredArgsLength, args.size()));
        }

        if (sub.getIsArgumentInvalid().test(strippedArgs.size())) {
            sub.sendUsage(sender, label);
            return CommandResult.INVALID_ARGS;
        }

        final String name = args.get(0).toLowerCase();
        T t = getTarget(name, plugin, sender);
        if (t != null) {
            CommandResult result = sub.execute(plugin, sender, t, strippedArgs, label);
            cleanup(t, plugin);
            return result;
        }

        return CommandResult.LOADING_ERROR;
    }

    /**
     * Gets the object the command is acting upon, and runs the callback if successful
     * @param target the name of the object to be looked up
     * @param plugin a link to the main plugin instance
     * @param sender the user who send the command (used to send error messages if the lookup was unsuccessful)
     */
    protected abstract T getTarget(String target, LuckPermsPlugin plugin, Sender sender);

    protected abstract void cleanup(T t, LuckPermsPlugin plugin);

    /**
     * Get a list of {@link T} objects for tab completion
     * @param plugin a link to the main plugin instance
     * @return a list of strings
     */
    protected abstract List<String> getObjects(LuckPermsPlugin plugin);

    /**
     * Send the command usage to a sender
     * @param sender the sender to send the usage to
     * @param label the command label used
     */
    protected void sendUsage(Sender sender, String label) {
        if (getSubCommands().isEmpty()) {
            Util.sendPluginMessage(sender, "&e-> &d" + String.format(getUsage(), label));
            return;
        }

        List<SubCommand> subs = getSubCommands().stream()
                .filter(s -> s.isAuthorized(sender))
                .collect(Collectors.toList());

        if (subs.size() > 0) {
            Util.sendPluginMessage(sender, "&e" + getName() + " Sub Commands:");

            for (SubCommand s : subs) {
                s.sendUsage(sender, label);
            }

        } else {
            Message.COMMAND_NO_PERMISSION.send(sender);
        }
    }

    /**
     * If a sender has permission to use this command
     * @param sender the sender trying to use the command
     * @return true if the sender can use the command
     */
    protected boolean isAuthorized(Sender sender) {
        return getSubCommands().stream().filter(sc -> sc.isAuthorized(sender)).count() != 0;
    }

    protected List<String> onTabComplete(Sender sender, List<String> args, LuckPermsPlugin plugin) {
        final List<String> objects = getObjects(plugin);

        if (args.size() <= 1) {
            if (args.isEmpty() || args.get(0).equalsIgnoreCase("")) {
                return objects;
            }

            return objects.stream()
                    .filter(s -> s.toLowerCase().startsWith(args.get(0).toLowerCase()))
                    .collect(Collectors.toList());
        }

        final List<SubCommand<T>> subs = getSubCommands().stream()
                .filter(s -> s.isAuthorized(sender))
                .collect(Collectors.toList());

        if (args.size() == 2) {
            if (args.get(1).equalsIgnoreCase("")) {
                return subs.stream()
                        .map(m -> m.getName().toLowerCase())
                        .collect(Collectors.toList());
            }

            return subs.stream()
                    .map(m -> m.getName().toLowerCase())
                    .filter(s -> s.toLowerCase().startsWith(args.get(1).toLowerCase()))
                    .collect(Collectors.toList());
        }

        Optional<SubCommand<T>> o = subs.stream()
                .filter(s -> s.getName().equalsIgnoreCase(args.get(1)))
                .limit(1)
                .findAny();

        if (!o.isPresent()) {
            return Collections.emptyList();
        }

        return o.get().onTabComplete(plugin, sender, args.subList(2, args.size()));
    }
}
