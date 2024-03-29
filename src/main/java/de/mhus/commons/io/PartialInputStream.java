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
package de.mhus.commons.io;

import java.io.IOException;
import java.io.InputStream;

public class PartialInputStream extends InputStream {

    private int max;
    private InputStream src;
    private int cnt;
    private Integer delimiter;

    public PartialInputStream(InputStream pSrc, int length) {
        src = pSrc;
        max = length;
        cnt = 0;
    }

    public void setLength(int length) {
        cnt = 0;
        max = length;
    }

    public int getBytesLeft() {
        return max - cnt;
    }

    @Override
    public int read() throws IOException {
        if (max >= 0 && cnt >= max)
            return -1;
        cnt++;
        if (delimiter != null) {
            int ret = src.read();
            if (ret == delimiter) {
                max = 0;
                cnt = 0;
                return -1;
            }
        }
        return src.read();
    }

    public void setDelimiter(Integer d) {
        delimiter = d;
    }
}
