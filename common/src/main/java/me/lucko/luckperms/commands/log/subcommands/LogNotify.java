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

package me.lucko.luckperms.commands.log.subcommands;

import me.lucko.luckperms.LuckPermsPlugin;
import me.lucko.luckperms.commands.CommandResult;
import me.lucko.luckperms.commands.Predicate;
import me.lucko.luckperms.commands.Sender;
import me.lucko.luckperms.commands.SubCommand;
import me.lucko.luckperms.constants.Message;
import me.lucko.luckperms.constants.Permission;
import me.lucko.luckperms.data.Log;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public class LogNotify extends SubCommand<Log> {
    public LogNotify() {
        super("notify", "Toggle notifications", "/%s log notify [on|off]", Permission.LOG_NOTIFY,
                Predicate.notInRange(0, 1));
    }

    @Override
    public CommandResult execute(LuckPermsPlugin plugin, Sender sender, Log log, List<String> args, String label) {
        final Set<UUID> ignoring = plugin.getIgnoringLogs();
        final UUID uuid = sender.getUuid();
        if (args.size() == 0) {
            if (ignoring.contains(uuid)) {
                // toggle on
                ignoring.remove(uuid);
                Message.LOG_NOTIFY_TOGGLE_ON.send(sender);
                return CommandResult.SUCCESS;
            }
            // toggle off
            ignoring.add(uuid);
            Message.LOG_NOTIFY_TOGGLE_OFF.send(sender);
            return CommandResult.SUCCESS;
        }

        if (args.get(0).equalsIgnoreCase("on")) {
            if (!ignoring.contains(uuid)) {
                // already on
                Message.LOG_NOTIFY_ALREADY_ON.send(sender);
                return CommandResult.STATE_ERROR;
            }

            // toggle on
            ignoring.remove(uuid);
            Message.LOG_NOTIFY_TOGGLE_ON.send(sender);
            return CommandResult.SUCCESS;
        }

        if (args.get(0).equalsIgnoreCase("off")) {
            if (ignoring.contains(uuid)) {
                // already off
                Message.LOG_NOTIFY_ALREADY_OFF.send(sender);
                return CommandResult.STATE_ERROR;
            }

            // toggle off
            ignoring.add(uuid);
            Message.LOG_NOTIFY_TOGGLE_OFF.send(sender);
            return CommandResult.SUCCESS;
        }

        // not recognised
        Message.LOG_NOTIFY_UNKNOWN.send(sender);
        return CommandResult.INVALID_ARGS;
    }
}
