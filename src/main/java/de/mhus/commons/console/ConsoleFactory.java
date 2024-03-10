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
package de.mhus.commons.console;

import de.mhus.commons.annotations.service.DefaultImplementation;
import de.mhus.commons.services.IService;

import java.io.InputStream;
import java.io.PrintStream;

@DefaultImplementation(DefaultConsoleFactory.class)
public interface ConsoleFactory extends IService {

    public Console create(String term, InputStream in, PrintStream out);
}
