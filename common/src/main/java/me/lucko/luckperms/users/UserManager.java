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

package me.lucko.luckperms.users;

import lombok.RequiredArgsConstructor;
import me.lucko.luckperms.LuckPermsPlugin;
import me.lucko.luckperms.api.Node;
import me.lucko.luckperms.api.data.Callback;
import me.lucko.luckperms.exceptions.ObjectAlreadyHasException;
import me.lucko.luckperms.utils.AbstractManager;
import me.lucko.luckperms.utils.Identifiable;

import java.util.NoSuchElementException;
import java.util.UUID;

@RequiredArgsConstructor
public abstract class UserManager extends AbstractManager<UUID, User> {
    private final LuckPermsPlugin plugin;

    /**
     * Get a user object by name
     * @param name The name to search by
     * @return a {@link User} object if the user is loaded, returns null if the user is not loaded
     */
    @SuppressWarnings("OptionalGetWithoutIsPresent")
    public User get(String name) {
        try {
            return getAll().values().stream()
                    .filter(u -> u.getName().equalsIgnoreCase(name))
                    .limit(1).findAny().get();
        } catch (NoSuchElementException e) {
            return null;
        }
    }

    @Override
    public void preSet(User u) {
        giveDefaultIfNeeded(u, true);
    }

    @Override
    public void copy(User from, User to) {
        if (from.getPrimaryGroup() != null) {
            // This isn't just a black user. we shouldn't override in that case.
            to.setNodes(from.getNodes());
            to.setPrimaryGroup(from.getPrimaryGroup());
        }
        to.refreshPermissions();
    }

    /**
     * Set a user to the default group
     * @param user the user to give to
     */
    public void giveDefaultIfNeeded(User user, boolean save) {
        boolean hasGroup = false;
        for (Node node : user.getPermissions(false)) {
            if (node.isGroupNode()) {
                hasGroup = true;
                break;
            }
        }

        if (!hasGroup) {
            user.setPrimaryGroup("default");
            try {
                user.setPermission("group.default", true);
            } catch (ObjectAlreadyHasException ignored) {
                ignored.printStackTrace();
            }

            if (save) {
                plugin.getDatastore().saveUser(user, Callback.empty());
            }
        }
    }

    public boolean shouldSave(User user) {
        if (user.getNodes().size() != 1) {
            return true;
        }

        for (Node node : user.getNodes()) {
            // There's only one.
            if (!node.isGroupNode()) {
                return true;
            }

            if (node.isTemporary() || node.isServerSpecific() || node.isWorldSpecific()) {
                return true;
            }

            if (!node.getGroupName().equalsIgnoreCase("default")) {
                // The user's only node is not the default group one.
                return true;
            }
        }

        if (!user.getPrimaryGroup().equalsIgnoreCase("default")) {
            return true; // Not in the default primary group
        }

        return false;
    }

    /**
     * Checks to see if the user is online, and if they are not, runs {@link #unload(Identifiable)}
     * @param user The user to be cleaned up
     */
    public abstract void cleanup(User user);

    /**
     * Makes a new {@link User} object
     * @param uuid The UUID of the user
     * @param username The username of the user
     * @return a new {@link User} object
     */
    public abstract User make(UUID uuid, String username);

    /**
     * Reloads the data of all online users
     */
    public abstract void updateAllUsers();
}
