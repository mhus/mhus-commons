/**
 * Copyright (C) 2002 Mike Hummel (mh@mhus.de)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.mhus.commons.services;

import de.mhus.commons.tools.MCast;

public class DefaultEnvironmentProvider implements EnvironmentProvider {
    @Override
    public long getEnv(Class<?> owner, String name, long def) {
        return MCast.tolong(System.getenv(toEnvName(owner, name)), def);
    }

    private String toEnvName(Class<?> owner, String name) {
        return owner.getName().toUpperCase().replace('.', '_') + "_" + name.toUpperCase();
    }

    @Override
    public int getEnv(Class<?> owner, String name, int def) {
        return MCast.toint(System.getenv(toEnvName(owner, name)), def);
    }

    @Override
    public String getEnv(Class<?> owner, String name, String def) {
        final var value = System.getenv(toEnvName(owner, name));
        return value != null ? value : def;
    }
}
