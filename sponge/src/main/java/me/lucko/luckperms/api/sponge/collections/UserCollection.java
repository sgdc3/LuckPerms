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

package me.lucko.luckperms.api.sponge.collections;

import lombok.AllArgsConstructor;
import lombok.NonNull;
import me.lucko.luckperms.api.sponge.LuckPermsService;
import me.lucko.luckperms.api.sponge.LuckPermsSubject;
import me.lucko.luckperms.api.sponge.simple.SimpleSubject;
import me.lucko.luckperms.core.PermissionHolder;
import me.lucko.luckperms.users.User;
import me.lucko.luckperms.users.UserManager;
import org.spongepowered.api.service.context.Context;
import org.spongepowered.api.service.permission.PermissionService;
import org.spongepowered.api.service.permission.Subject;
import org.spongepowered.api.service.permission.SubjectCollection;
import org.spongepowered.api.service.permission.SubjectData;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@AllArgsConstructor
public class UserCollection implements SubjectCollection {
    private final LuckPermsService service;
    private final UserManager manager;

    @Override
    public String getIdentifier() {
        return PermissionService.SUBJECTS_USER;
    }

    @Override
    public Subject get(@NonNull String id) {
        PermissionHolder holder = null;
        try {
            UUID u = UUID.fromString(id);
            if (manager.isLoaded(u)) {
                holder = manager.get(u);
            }

        } catch (IllegalArgumentException e) {
            User user = manager.get(id);
            if (user != null) {
                holder = user;
            }
        }

        if (holder != null) {
            return LuckPermsSubject.wrapHolder(holder, service);
        }

        service.getPlugin().getLog().warn("Couldn't get subject for: " + id);

        // What am I meant to do here? What if no user is loaded? Load it? Create it?
        // If I do load/create it, this method should always be called async??.... errr.
        return new SimpleSubject(id, service, this);
    }

    @Override
    public boolean hasRegistered(@NonNull String id) {
        try {
            UUID u = UUID.fromString(id);
            return manager.isLoaded(u);
        } catch (IllegalArgumentException e) {
            User user = manager.get(id);
            return user != null;
        }
    }

    @Override
    public Iterable<Subject> getAllSubjects() {
        return manager.getAll().values().stream()
                .map(u -> LuckPermsSubject.wrapHolder(u, service))
                .collect(Collectors.toList());
    }

    @Override
    public Map<Subject, Boolean> getAllWithPermission(@NonNull String id) {
        return getAllWithPermission(SubjectData.GLOBAL_CONTEXT, id);
    }

    @Override
    public Map<Subject, Boolean> getAllWithPermission(@NonNull Set<Context> contexts, @NonNull String node) {
        return manager.getAll().values().stream()
                .map(u -> LuckPermsSubject.wrapHolder(u, service))
                .filter(sub -> sub.hasPermission(contexts, node))
                .collect(Collectors.toMap(sub -> sub, sub -> sub.getPermissionValue(contexts, node).asBoolean()));
    }

    @Override
    public Subject getDefaults() {
        return new SimpleSubject("default", service, this);
    }
}
