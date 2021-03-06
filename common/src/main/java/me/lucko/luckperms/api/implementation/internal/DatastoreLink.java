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

package me.lucko.luckperms.api.implementation.internal;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import me.lucko.luckperms.LuckPermsPlugin;
import me.lucko.luckperms.api.*;
import me.lucko.luckperms.api.data.Callback;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static me.lucko.luckperms.api.implementation.internal.Utils.*;

/**
 * Provides a link between {@link Datastore} and {@link me.lucko.luckperms.storage.Datastore}
 */
@SuppressWarnings({"unused", "WeakerAccess"})
public class DatastoreLink implements Datastore {

    private final LuckPermsPlugin plugin;
    private final me.lucko.luckperms.storage.Datastore master;
    private final Async async;
    private final Sync sync;
    private final Future future;

    public DatastoreLink(@NonNull LuckPermsPlugin plugin, @NonNull me.lucko.luckperms.storage.Datastore master) {
        this.plugin = plugin;
        this.master = master;
        this.async = new Async(master);
        this.sync = new Sync(master);
        this.future = new Future(master);
    }

    private static <T> Callback<T> checkCallback(Callback<T> c) {
        // If no callback was given, just send an empty one
        if (c == null) {
            c = Callback.empty();
        }
        return c;
    }

    @Override
    public String getName() {
        return master.getName();
    }

    @Override
    public boolean isAcceptingLogins() {
        return master.isAcceptingLogins();
    }

    @Override
    public Datastore.Async async() {
        return async;
    }

    @Override
    public Datastore.Sync sync() {
        return sync;
    }

    @Override
    public Datastore.Future future() {
        return future;
    }

    @AllArgsConstructor
    public class Async implements Datastore.Async {
        private final me.lucko.luckperms.storage.Datastore master;

        @Override
        public void logAction(@NonNull LogEntry entry, Callback<Boolean> callback) {
            master.logAction(entry, checkCallback(callback));
        }

        @Override
        public void getLog(Callback<Log> callback) {
            master.getLog(log -> callback.onComplete(new LogLink(log)));
        }

        @Override
        public void loadOrCreateUser(@NonNull UUID uuid, @NonNull String username, Callback<Boolean> callback) {
            master.loadUser(uuid, checkUsername(username), checkCallback(callback));
        }

        @Override
        public void loadUser(@NonNull UUID uuid, Callback<Boolean> callback) {
            master.loadUser(uuid, "null", checkCallback(callback));
        }

        @Override
        public void loadUser(@NonNull UUID uuid, @NonNull String username, Callback<Boolean> callback) {
            master.loadUser(uuid, checkUsername(username), checkCallback(callback));
        }

        @Override
        public void saveUser(@NonNull User user, Callback<Boolean> callback) {
            checkUser(user);
            master.saveUser(((UserLink) user).getMaster(), checkCallback(callback));
        }

        @Override
        public void cleanupUsers(Callback<Boolean> callback) {
            master.cleanupUsers(checkCallback(callback));
        }

        @Override
        public void getUniqueUsers(Callback<Set<UUID>> callback) {
            master.getUniqueUsers(checkCallback(callback));
        }

        @Override
        public void createAndLoadGroup(@NonNull String name, Callback<Boolean> callback) {
            master.createAndLoadGroup(checkName(name), checkCallback(callback));
        }

        @Override
        public void loadGroup(@NonNull String name, Callback<Boolean> callback) {
            master.loadGroup(checkName(name), checkCallback(callback));
        }

        @Override
        public void loadAllGroups(Callback<Boolean> callback) {
            master.loadAllGroups(checkCallback(callback));
        }

        @Override
        public void saveGroup(@NonNull Group group, Callback<Boolean> callback) {
            checkGroup(group);
            master.saveGroup(((GroupLink) group).getMaster(), checkCallback(callback));
        }

        @Override
        public void deleteGroup(@NonNull Group group, Callback<Boolean> callback) {
            checkGroup(group);
            if (group.getName().equalsIgnoreCase(plugin.getConfiguration().getDefaultGroupName())) {
                throw new IllegalArgumentException("Cannot delete the default group.");
            }
            master.deleteGroup(((GroupLink) group).getMaster(), checkCallback(callback));
        }

        @Override
        public void createAndLoadTrack(@NonNull String name, Callback<Boolean> callback) {
            master.createAndLoadTrack(checkName(name), checkCallback(callback));
        }

        @Override
        public void loadTrack(@NonNull String name, Callback<Boolean> callback) {
            master.loadTrack(checkName(name), checkCallback(callback));
        }

        @Override
        public void loadAllTracks(Callback<Boolean> callback) {
            master.loadAllTracks(checkCallback(callback));
        }

        @Override
        public void saveTrack(@NonNull Track track, Callback<Boolean> callback) {
            checkTrack(track);
            master.saveTrack(((TrackLink) track).getMaster(), checkCallback(callback));
        }

        @Override
        public void deleteTrack(@NonNull Track track, Callback<Boolean> callback) {
            checkTrack(track);
            master.deleteTrack(((TrackLink) track).getMaster(), checkCallback(callback));
        }

        @Override
        public void saveUUIDData(@NonNull String username, @NonNull UUID uuid, Callback<Boolean> callback) {
            master.saveUUIDData(checkUsername(username), uuid, checkCallback(callback));
        }

        @Override
        public void getUUID(@NonNull String username, Callback<UUID> callback) {
            master.getUUID(checkUsername(username), checkCallback(callback));
        }
    }

    @AllArgsConstructor
    public class Sync implements Datastore.Sync {
        private final me.lucko.luckperms.storage.Datastore master;

        @Override
        public boolean logAction(@NonNull LogEntry entry) {
            return master.logAction(entry);
        }

        @Override
        public Log getLog() {
            return new LogLink(master.getLog());
        }

        @Override
        public boolean loadOrCreateUser(@NonNull UUID uuid, @NonNull String username) {
            return master.loadUser(uuid, checkUsername(username));
        }

        @Override
        public boolean loadUser(@NonNull UUID uuid) {
            return master.loadUser(uuid, "null");
        }

        @Override
        public boolean loadUser(@NonNull UUID uuid, @NonNull String username) {
            return master.loadUser(uuid, checkUsername(username));
        }

        @Override
        public boolean saveUser(@NonNull User user) {
            checkUser(user);
            return master.saveUser(((UserLink) user).getMaster());
        }

        @Override
        public boolean cleanupUsers() {
            return master.cleanupUsers();
        }

        @Override
        public Set<UUID> getUniqueUsers() {
            return master.getUniqueUsers();
        }

        @Override
        public boolean createAndLoadGroup(@NonNull String name) {
            return master.createAndLoadGroup(checkName(name));
        }

        @Override
        public boolean loadGroup(@NonNull String name) {
            return master.loadGroup(checkName(name));
        }

        @Override
        public boolean loadAllGroups() {
            return master.loadAllGroups();
        }

        @Override
        public boolean saveGroup(@NonNull Group group) {
            checkGroup(group);
            return master.saveGroup(((GroupLink) group).getMaster());
        }

        @Override
        public boolean deleteGroup(@NonNull Group group) {
            checkGroup(group);
            if (group.getName().equalsIgnoreCase(plugin.getConfiguration().getDefaultGroupName())) {
                throw new IllegalArgumentException("Cannot delete the default group.");
            }
            return master.deleteGroup(((GroupLink) group).getMaster());
        }

        @Override
        public boolean createAndLoadTrack(@NonNull String name) {
            return master.createAndLoadTrack(checkName(name));
        }

        @Override
        public boolean loadTrack(@NonNull String name) {
            return master.loadTrack(checkName(name));
        }

        @Override
        public boolean loadAllTracks() {
            return master.loadAllTracks();
        }

        @Override
        public boolean saveTrack(@NonNull Track track) {
            checkTrack(track);
            return master.saveTrack(((TrackLink) track).getMaster());
        }

        @Override
        public boolean deleteTrack(@NonNull Track track) {
            checkTrack(track);
            return master.deleteTrack(((TrackLink) track).getMaster());
        }

        @Override
        public boolean saveUUIDData(@NonNull String username, @NonNull UUID uuid) {
            return master.saveUUIDData(checkUsername(username), uuid);
        }

        @Override
        public UUID getUUID(@NonNull String username) {
            return master.getUUID(checkUsername(username));
        }
    }

    @AllArgsConstructor
    public class Future implements Datastore.Future {
        private final me.lucko.luckperms.storage.Datastore master;

        @Override
        public java.util.concurrent.Future<Boolean> logAction(@NonNull LogEntry entry) {
            LPFuture<Boolean> lpf = new LPFuture<>();
            master.logAction(entry, lpf);
            return lpf;
        }

        @Override
        public java.util.concurrent.Future<Log> getLog() {
            LPFuture<Log> lpf = new LPFuture<>();
            master.getLog(log -> lpf.onComplete(new LogLink(log)));
            return lpf;
        }

        @Override
        public java.util.concurrent.Future<Boolean> loadOrCreateUser(@NonNull UUID uuid, @NonNull String username) {
            LPFuture<Boolean> lpf = new LPFuture<>();
            master.loadUser(uuid, checkUsername(username), lpf);
            return lpf;
        }

        @Override
        public java.util.concurrent.Future<Boolean> loadUser(@NonNull UUID uuid) {
            LPFuture<Boolean> lpf = new LPFuture<>();
            master.loadUser(uuid, "null", lpf);
            return lpf;
        }

        @Override
        public java.util.concurrent.Future<Boolean> loadUser(@NonNull UUID uuid, @NonNull String username) {
            LPFuture<Boolean> lpf = new LPFuture<>();
            master.loadUser(uuid, checkUsername(username), lpf);
            return lpf;
        }

        @Override
        public java.util.concurrent.Future<Boolean> saveUser(@NonNull User user) {
            LPFuture<Boolean> lpf = new LPFuture<>();
            checkUser(user);
            master.saveUser(((UserLink) user).getMaster(), lpf);
            return lpf;
        }

        @Override
        public java.util.concurrent.Future<Boolean> cleanupUsers() {
            LPFuture<Boolean> lpf = new LPFuture<>();
            master.cleanupUsers(lpf);
            return lpf;
        }

        @Override
        public java.util.concurrent.Future<Set<UUID>> getUniqueUsers() {
            LPFuture<Set<UUID>> lpf = new LPFuture<>();
            master.getUniqueUsers(lpf);
            return lpf;
        }

        @Override
        public java.util.concurrent.Future<Boolean> createAndLoadGroup(@NonNull String name) {
            LPFuture<Boolean> lpf = new LPFuture<>();
            master.createAndLoadGroup(checkName(name), lpf);
            return lpf;
        }

        @Override
        public java.util.concurrent.Future<Boolean> loadGroup(@NonNull String name) {
            LPFuture<Boolean> lpf = new LPFuture<>();
            master.loadGroup(checkName(name), lpf);
            return lpf;
        }

        @Override
        public java.util.concurrent.Future<Boolean> loadAllGroups() {
            LPFuture<Boolean> lpf = new LPFuture<>();
            master.loadAllGroups(lpf);
            return lpf;
        }

        @Override
        public java.util.concurrent.Future<Boolean> saveGroup(@NonNull Group group) {
            LPFuture<Boolean> lpf = new LPFuture<>();
            checkGroup(group);
            master.saveGroup(((GroupLink) group).getMaster(), lpf);
            return lpf;
        }

        @Override
        public java.util.concurrent.Future<Boolean> deleteGroup(@NonNull Group group) {
            LPFuture<Boolean> lpf = new LPFuture<>();
            checkGroup(group);
            if (group.getName().equalsIgnoreCase(plugin.getConfiguration().getDefaultGroupName())) {
                throw new IllegalArgumentException("Cannot delete the default group.");
            }
            master.deleteGroup(((GroupLink) group).getMaster(), lpf);
            return lpf;
        }

        @Override
        public java.util.concurrent.Future<Boolean> createAndLoadTrack(@NonNull String name) {
            LPFuture<Boolean> lpf = new LPFuture<>();
            master.createAndLoadTrack(checkName(name), lpf);
            return lpf;
        }

        @Override
        public java.util.concurrent.Future<Boolean> loadTrack(@NonNull String name) {
            LPFuture<Boolean> lpf = new LPFuture<>();
            master.loadTrack(checkName(name), lpf);
            return lpf;
        }

        @Override
        public java.util.concurrent.Future<Boolean> loadAllTracks() {
            LPFuture<Boolean> lpf = new LPFuture<>();
            master.loadAllTracks(lpf);
            return lpf;
        }

        @Override
        public java.util.concurrent.Future<Boolean> saveTrack(@NonNull Track track) {
            LPFuture<Boolean> lpf = new LPFuture<>();
            checkTrack(track);
            master.saveTrack(((TrackLink) track).getMaster(), lpf);
            return lpf;
        }

        @Override
        public java.util.concurrent.Future<Boolean> deleteTrack(@NonNull Track track) {
            LPFuture<Boolean> lpf = new LPFuture<>();
            checkTrack(track);
            master.deleteTrack(((TrackLink) track).getMaster(), lpf);
            return lpf;
        }

        @Override
        public java.util.concurrent.Future<Boolean> saveUUIDData(@NonNull String username, @NonNull UUID uuid) {
            LPFuture<Boolean> lpf = new LPFuture<>();
            master.saveUUIDData(checkUsername(username), uuid, lpf);
            return lpf;
        }

        @Override
        public java.util.concurrent.Future<UUID> getUUID(@NonNull String username) {
            LPFuture<UUID> lpf = new LPFuture<>();
            master.getUUID(checkUsername(username), lpf);
            return lpf;
        }
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class LPFuture<T> implements Callback<T>, java.util.concurrent.Future<T> {
        private final CountDownLatch latch = new CountDownLatch(1);
        private T value;

        @Override
        public void onComplete(T t) {
            value = t;
            latch.countDown();
        }

        @Override
        public boolean cancel(boolean mayInterruptIfRunning) {
            // Not supported
            return false;
        }

        @Override
        public boolean isCancelled() {
            return false;
        }

        @Override
        public boolean isDone() {
            return latch.getCount() == 0;
        }

        @Override
        public T get() throws InterruptedException {
            latch.await();
            return value;
        }

        @Override
        public T get(long timeout, TimeUnit unit) throws InterruptedException, TimeoutException {
            if (latch.await(timeout, unit)) {
                return value;
            } else {
                throw new TimeoutException();
            }
        }
    }

}
