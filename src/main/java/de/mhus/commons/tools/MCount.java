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

import de.mhus.commons.services.MService;
import de.mhus.commons.services.UniqueId;
import lombok.extern.slf4j.Slf4j;

import java.io.Closeable;

@Slf4j
public class MCount implements Closeable {

    protected long cnt;
    private String name;
    private long startTime = 0;
    private long lastTime = 0;
    protected boolean isClosed;

    public MCount() {
        cnt = 0;
        name = "Counter " + MService.getService(UniqueId.class).nextUniqueId();
    }

    public MCount(String name) {
        cnt = 0;
        this.name = name;
    }

    public void reset() {
        isClosed = false;
        cnt = 0;
        startTime = 0;
        lastTime = 0;
    }

    public void inc() {
        if (isClosed) return;
        cnt++;
        lastTime = System.currentTimeMillis();
        if (startTime == 0) startTime = lastTime;
    }

    public long getValue() {
        return cnt;
    }

    public double getHitsPerSecond() {
        if (startTime == 0 || lastTime == 0 || cnt == 0) return 0;
        return (double) cnt / (double) ((lastTime - startTime) / 1000);
    }

    public String getName() {
        return name;
    }

    public long getFirstHitTime() {
        return startTime;
    }

    public long getLastHitTime() {
        return lastTime;
    }

    public String getStatusAsString() {
        if (startTime == 0 || lastTime == 0 || cnt == 0) return "unused";
        return MDate.toIsoDateTime(getFirstHitTime())
                + " - "
                + MDate.toIsoDateTime(getLastHitTime())
                + ","
                + getHitsPerSecond()
                + " hits/sec,"
                + cnt;
    }

    public void close() {
        if (isClosed) return;
        isClosed = true;
        LOGGER.debug("close {} {} {}", name, cnt, getHitsPerSecond());
    }

    @Override
    public String toString() {
        return MSystem.toString(this, getStatusAsString());
    }
}
