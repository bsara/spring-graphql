/*
 * Copyright 2002-2023 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.graphql.execution;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;

import org.dataloader.BatchLoaderEnvironment;
import org.dataloader.DataLoaderOptions;
import org.springframework.lang.Nullable;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Provides a way to create batch loader beans.
 * TODO
 *
 * @param <K>
 * @param <V>
 */
public sealed interface FunctionBatchLoaderSpec<K, V> permits DefaultFunctionBatchLoaderSpec {

    /**
     * @return name to be used for the {@link org.dataloader.DataLoader}
     * that will be created for this spec.
     */
    String getName();

    /**
     * @return data loader options to be used for the
     * {@link org.dataloader.DataLoader} that will be created for this spec.
     */
    @Nullable
    DataLoaderOptions getDataLoaderOptions();

    /**
     * @return TODO
     */
    @Nullable
    BiFunction<List<K>, BatchLoaderEnvironment, Flux<V>> getNonMappedLoader();

    /**
     * @return TODO
     */
    @Nullable
    BiFunction<Set<K>, BatchLoaderEnvironment, Mono<Map<K, V>>> getMappedLoader();

    /**
     * @return true, if the loader is a mapped loader
     */
    boolean isMapped();

    static <K, V> FunctionBatchLoaderSpec<K, V> create(String name, BiFunction<List<K>, BatchLoaderEnvironment, Flux<V>> loader) {
        return new DefaultFunctionBatchLoaderSpec<>(name, loader, false);
    }

    static <K, V> FunctionBatchLoaderSpec<K, V> create(String name, @Nullable DataLoaderOptions options, BiFunction<List<K>, BatchLoaderEnvironment, Flux<V>> loader) {
        return new DefaultFunctionBatchLoaderSpec<>(name, options, loader, false);
    }

    static <K, V> FunctionBatchLoaderSpec<K, V> createMapped(String name, BiFunction<Set<K>, BatchLoaderEnvironment, Mono<Map<K, V>>> loader) {
        return new DefaultFunctionBatchLoaderSpec<>(name, loader, true);
    }

    static <K, V> FunctionBatchLoaderSpec<K, V> createMapped(String name, @Nullable DataLoaderOptions options, BiFunction<Set<K>, BatchLoaderEnvironment, Mono<Map<K, V>>> loader) {
        return new DefaultFunctionBatchLoaderSpec<>(name, options, loader, true);
    }
}
