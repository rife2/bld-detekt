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

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ReportIdTests {

    @Test
    @DisplayName("Should have 4 report IDs")
    void count() {
        assertThat(ReportId.values()).hasSize(4);
    }

    @Test
    @DisplayName("Should contain all expected report IDs in the correct order")
    void enumValues() {
        assertThat(ReportId.values()).containsExactly(
                ReportId.CHECKSTYLE,
                ReportId.HTML,
                ReportId.MARKDOWN,
                ReportId.SARIF
        );
    }

    @ParameterizedTest
    @EnumSource(ReportId.class)
    @DisplayName("name() and toString() should be non-blank and uppercase")
    void nameAndToString(ReportId reportId) {
        assertThat(reportId.name()).isNotBlank().isUpperCase();
        assertThat(reportId.toString()).isNotBlank();
    }

    @ParameterizedTest
    @EnumSource(ReportId.class)
    @DisplayName("ordinal should be unique and match declaration order")
    void ordinal(ReportId reportId) {
        assertThat(reportId.ordinal()).isGreaterThanOrEqualTo(0).isLessThan(ReportId.values().length);
        assertThat(ReportId.values()[reportId.ordinal()]).isEqualTo(reportId);
    }

    @ParameterizedTest
    @EnumSource(ReportId.class)
    @DisplayName("The valueOf() method should return the correct enum constant")
    void valueOf(ReportId reportId) {
        assertThat(ReportId.valueOf(reportId.name())).isEqualTo(reportId);
    }

    @Test
    @DisplayName("The valueOf() method should throw an exception for an invalid ID")
    void valueOfInvalid() {
        assertThatThrownBy(() -> ReportId.valueOf("JSON"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("No enum constant rife.bld.extension.detekt.ReportId.JSON");
    }

    @Test
    @DisplayName("The valueOf() method should throw NPE for null")
    void valueOfNull() {
        assertThatThrownBy(() -> ReportId.valueOf(null))
                .isInstanceOf(NullPointerException.class);
    }
}