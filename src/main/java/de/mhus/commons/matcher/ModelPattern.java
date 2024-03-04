/**
 * Copyright (C) 2022 Mike Hummel (mh@mhus.de)
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
package de.mhus.commons.matcher;

public abstract class ModelPattern extends ModelPart {

    public enum CONDITION {
        NONE,
        LT,
        LE,
        EQ,
        GE,
        GR
    }

    private CONDITION condition;

    public abstract void setPattern(String pattern);

    public abstract String getPattern();

    public abstract String getPatternStr();

    public abstract String getPatternTypeName();

    @Override
    public String toString() {
        return (getParamName() != null ? "${" + getParamName() + "} " : "")
                + (isNot() ? "!" : "")
                + (getCondition() == null || getCondition() == CONDITION.NONE
                        ? ""
                        : getCondition() + " ")
                + getPatternTypeName()
                + " "
                + getPatternStr();
    }

    public void setCondition(CONDITION cond) {
        this.condition = cond;
    }

    public CONDITION getCondition() {
        return condition;
    }
}