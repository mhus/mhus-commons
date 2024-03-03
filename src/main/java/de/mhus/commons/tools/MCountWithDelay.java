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
package de.mhus.commons.tools;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MCountWithDelay extends MCount {

    private long sleepInterval = 0;
    private int sleepSeconds = 0;
    private boolean throwExceptionOnNextCount = false;

    public MCountWithDelay() {
        super();
    }

    public MCountWithDelay(String name) {
        super(name);
    }

    public long getSleepInterval() {
        return sleepInterval;
    }

    public void setSleepInterval(long sleepInterval) {
        this.sleepInterval = sleepInterval;
    }

    public int getSleepSeconds() {
        return sleepSeconds;
    }

    public void setSleepSeconds(int sleepSeconds) {
        this.sleepSeconds = sleepSeconds;
    }

    @Override
    public void inc() {
        super.inc();
        if (throwExceptionOnNextCount) {
            throwExceptionOnNextCount = false;
            throw new RuntimeException(
                    "Counter " + getName() + " is thrown by request at " + getValue());
        }
        if (isClosed) return;
        if (sleepInterval > 0 && sleepSeconds > 0 && cnt % sleepInterval == 0) {
            LOGGER.debug("Sleep {} {}",getName(),  sleepSeconds);
            MThread.sleep(sleepSeconds * 1000);
        }
    }

    public boolean isThrowExceptionOnNextCount() {
        return throwExceptionOnNextCount;
    }

    public void setThrowExceptionOnNextCount(boolean throwExceptionOnNextCount) {
        this.throwExceptionOnNextCount = throwExceptionOnNextCount;
    }
}
