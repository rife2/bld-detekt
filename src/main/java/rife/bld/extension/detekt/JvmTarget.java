/*
 * Copyright 2023-2026 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package rife.bld.extension.detekt;

/**
 * Target version of the generated JVM bytecode.
 *
 * @author <a href="https://erik.thauvin.net/">Erik C. Thauvin</a>
 * @since 2.0
 */
public enum JvmTarget {
    JVM_1_6("1.6"),
    JVM_1_8("1.8"),
    JVM_9("9"),
    JVM_10("10"),
    JVM_11("11"),
    JVM_12("12"),
    JVM_13("13"),
    JVM_14("14"),
    JVM_15("15"),
    JVM_16("16"),
    JVM_17("17"),
    JVM_18("18"),
    JVM_19("19"),
    JVM_20("20"),
    JVM_21("21"),
    JVM_22("22"),
    JVM_23("23"),
    JVM_24("24"),
    JVM_25("25"),
    JVM_26("26");

    private final String version;

    JvmTarget(String version) {
        this.version = version;
    }

    @Override
    public String toString() {
        return version;
    }

    public String version() {
        return version;
    }
}