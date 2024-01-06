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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Supplier;

import graphql.GraphQLContext;
import io.micrometer.context.ContextSnapshot;

import org.dataloader.BatchLoader;
import org.dataloader.BatchLoaderContextProvider;
import org.dataloader.BatchLoaderEnvironment;
import org.dataloader.BatchLoaderWithContext;
import org.dataloader.DataLoader;
import org.dataloader.DataLoaderFactory;
import org.dataloader.DataLoaderOptions;
import org.dataloader.DataLoaderRegistry;
import org.dataloader.MappedBatchLoader;
import org.dataloader.MappedBatchLoaderWithContext;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

/**
 * Default implementation of {@link BatchLoaderRegistry} that stores batch loader
 * registrations. Also, an implementation of {@link DataLoaderRegistrar} that
 * registers the batch loaders as {@link DataLoader}s in {@link DataLoaderRegistry}.
 *
 * @author Rossen Stoyanchev
 * @since 1.0.0
 */
public class DefaultBatchLoaderRegistry implements BatchLoaderRegistry {

    private static final BatchLoaderContextProvider DEFAULT_NULL_BATCH_LOADER_CONTEXT_PROVIDER = DataLoaderOptions.newOptions().getBatchLoaderContextProvider();

	private final List<ReactorBatchLoader<?,?>> loaders = new ArrayList<>();

	private final List<ReactorMappedBatchLoader<?,?>> mappedLoaders = new ArrayList<>();

    private final List<BatchLoaderSpec> specs = new ArrayList<>();

    private final BatchLoaderContextProviderFactory defaultBatchLoaderContextProviderFactory;

	private final Supplier<DataLoaderOptions> defaultOptionsSupplier;


	/**
	 * Default constructor.
	 */
	public DefaultBatchLoaderRegistry() {
		this(DataLoaderOptions::newOptions, null);
	}

	/**
	 * Constructor with a default {@link DataLoaderOptions} supplier to use as
	 * a starting point for batch loader registrations.
	 * @since 1.1.0
	 */
	public DefaultBatchLoaderRegistry(@NonNull Supplier<DataLoaderOptions> defaultOptionsSupplier) {
		this(defaultOptionsSupplier, null);
	}

    /**
     * TODO
     */
    public DefaultBatchLoaderRegistry(@NonNull Supplier<DataLoaderOptions> defaultOptionsSupplier, @Nullable BatchLoaderContextProviderFactory defaultBatchLoaderContextProviderFactory) {
        Assert.notNull(defaultOptionsSupplier, "'defaultOptionsSupplier' is required");

        this.defaultOptionsSupplier = defaultOptionsSupplier;
        this.defaultBatchLoaderContextProviderFactory = (defaultBatchLoaderContextProviderFactory != null ? defaultBatchLoaderContextProviderFactory : ((context) -> (() -> context)));
    }


    @Override
	public <K, V> RegistrationSpec<K, V> forTypePair(Class<K> keyType, Class<V> valueType) {
		return new DefaultRegistrationSpec<>(valueType);
	}

    @Override
	public <K, V> RegistrationSpec<K, V> forName(String name) {
		return new DefaultRegistrationSpec<>(name);
	}

    @Override
    public <K, V> BatchLoaderRegistry register(FunctionBatchLoaderSpec<K, V> spec) {
        Assert.notNull(spec, "'spec' is required");

        return (spec.isMapped()
            ? this.registerMapped(spec.getName(), spec.getDataLoaderOptions(), spec.getMappedLoader())
            : this.register(spec.getName(), spec.getDataLoaderOptions(), spec.getNonMappedLoader()));
    }

    @Override
    public <K, V> BatchLoaderRegistry register(String name, @Nullable DataLoaderOptions options, BiFunction<List<K>, BatchLoaderEnvironment, Flux<V>> loader) {
        Assert.notNull(name, "'name' is required");
        Assert.notNull(loader, "'loader' is required");

        DefaultRegistrationSpec<K, V> spec = new DefaultRegistrationSpec<>(name);
        spec.withOptions(options);
        spec.registerBatchLoader(loader);

        return this;
    }

    @Override
    public <K, V> BatchLoaderRegistry registerMapped(String name, DataLoaderOptions options, BiFunction<Set<K>, BatchLoaderEnvironment, Mono<Map<K, V>>> loader) {
        Assert.notNull(name, "'name' is required");
        Assert.notNull(loader, "'loader' is required");

        DefaultRegistrationSpec<K, V> spec = new DefaultRegistrationSpec<>(name);
        spec.withOptions(options);
        spec.registerMappedBatchLoader(loader);

        return this;
    }

    @Override
    public BatchLoaderRegistry register(BatchLoaderSpec spec) {
        Assert.notNull(spec, "'spec' is required");
        this.specs.add(spec);
        return this;
    }

    @Override
	public void registerDataLoaders(DataLoaderRegistry registry, GraphQLContext context) {
        BatchLoaderContextProvider defaultContextProvider = defaultBatchLoaderContextProviderFactory.create(context);

		for (ReactorBatchLoader<?, ?> loader : this.loaders) {
            this.validateDataLoaderNameIsUnique(registry, loader.getName());
			DataLoaderOptions options = createDataLoaderOptions(loader.getOptions());
			options = options.setBatchLoaderContextProvider(defaultContextProvider);
			DataLoader<?, ?> dataLoader = DataLoaderFactory.newDataLoader(loader, options);
            registry.register(loader.getName(), dataLoader);
		}

		for (ReactorMappedBatchLoader<?, ?> loader : this.mappedLoaders) {
            this.validateDataLoaderNameIsUnique(registry, loader.getName());
            DataLoaderOptions options = createDataLoaderOptions(loader.getOptions());
			options = options.setBatchLoaderContextProvider(defaultContextProvider);
			DataLoader<?, ?> dataLoader = DataLoaderFactory.newMappedDataLoader(loader, options);
			registry.register(loader.getName(), dataLoader);
		}

        for (BatchLoaderSpec spec : this.specs) {
            this.validateDataLoaderNameIsUnique(registry, spec.getName());

            Object batchLoader = spec.getLoader();
            DataLoaderOptions options = createDataLoaderOptions(spec, defaultContextProvider);

            DataLoader<?, ?> dataLoader;

            if (batchLoader instanceof BatchLoader<?, ?> bl) {
                dataLoader = DataLoaderFactory.newDataLoader(bl, options);
            } else if (batchLoader instanceof MappedBatchLoader<?, ?> bl) {
                dataLoader = DataLoaderFactory.newMappedDataLoader(bl, options);
            } else if (batchLoader instanceof BatchLoaderWithContext<?, ?> bl) {
                dataLoader = DataLoaderFactory.newDataLoader(bl, options);
            } else if (batchLoader instanceof MappedBatchLoaderWithContext<?, ?> bl) {
                dataLoader = DataLoaderFactory.newMappedDataLoader(bl, options);
            } else {
                throw new IllegalArgumentException("Batch loader provided is not of any batch loader type found in the dataloader library: '" + batchLoader.getClass().getCanonicalName() + "'");
            }

            registry.register(spec.getName(), dataLoader);
        }
	}

    private void validateDataLoaderNameIsUnique(DataLoaderRegistry registry, String dataLoaderName) {
        if (registry.getDataLoader(dataLoaderName) != null) {
            throw new IllegalStateException("More than one DataLoader named '" + dataLoaderName + "'");
        }
    }

    private DataLoaderOptions createDataLoaderOptions(BatchLoaderSpec spec, BatchLoaderContextProvider defaultContextProvider) {
        var options = this.createDataLoaderOptions(spec.getDataLoaderOptions());

        var batchLoader = spec.getLoader();

        if ((batchLoader instanceof BatchLoaderWithContext<?, ?> || batchLoader instanceof MappedBatchLoaderWithContext<?, ?>)
            && options.getBatchLoaderContextProvider() == DEFAULT_NULL_BATCH_LOADER_CONTEXT_PROVIDER) {
            options.setBatchLoaderContextProvider(defaultContextProvider);
        }

        return options;
    }

    private DataLoaderOptions createDataLoaderOptions(@Nullable DataLoaderOptions options) {
        return (options == null ? new DataLoaderOptions(defaultOptionsSupplier.get()) : options);
    }


	private class DefaultRegistrationSpec<K, V> implements RegistrationSpec<K, V> {

		@Nullable
		private final Class<?> valueType;

		@Nullable
		private String name;

		@Nullable
		private DataLoaderOptions options;

		@Nullable
		private Consumer<DataLoaderOptions> optionsConsumer;

		public DefaultRegistrationSpec(Class<V> valueType) {
			this.valueType = valueType;
		}

		public DefaultRegistrationSpec(String name) {
			this.name = name;
			this.valueType = null;
		}

        @Override
		public RegistrationSpec<K, V> withName(String name) {
			this.name = name;
			return this;
		}

        @Override
		public RegistrationSpec<K, V> withOptions(@Nullable Consumer<DataLoaderOptions> optionsConsumer) {
			this.optionsConsumer = (this.optionsConsumer != null ?
					this.optionsConsumer.andThen(optionsConsumer) : optionsConsumer);
			return this;
		}

        @NonNull
        @Override
		public RegistrationSpec<K, V> withOptions(@Nullable DataLoaderOptions options) {
			this.options = options;
			return this;
		}

		@Override
		public void registerBatchLoader(BiFunction<List<K>, BatchLoaderEnvironment, Flux<V>> loader) {
			DefaultBatchLoaderRegistry.this.loaders.add(
					new ReactorBatchLoader<>(initName(), loader, initOptionsSupplier()));
		}

		@Override
		public void registerMappedBatchLoader(BiFunction<Set<K>, BatchLoaderEnvironment, Mono<Map<K, V>>> loader) {
			DefaultBatchLoaderRegistry.this.mappedLoaders.add(
					new ReactorMappedBatchLoader<>(initName(), loader, initOptionsSupplier()));
		}

		private String initName() {
			if (StringUtils.hasText(this.name)) {
				return this.name;
			}
			Assert.notNull(this.valueType, "Value type not available to select a default DataLoader name.");
			return (StringUtils.hasText(this.name) ? this.name : this.valueType.getName());
		}

		private Supplier<DataLoaderOptions> initOptionsSupplier() {

			Supplier<DataLoaderOptions> optionsSupplier = () ->
					new DataLoaderOptions(this.options != null ?
							this.options : DefaultBatchLoaderRegistry.this.defaultOptionsSupplier.get());

			if (this.optionsConsumer == null) {
				return optionsSupplier;
			}

			return () -> {
				DataLoaderOptions options = optionsSupplier.get();
				this.optionsConsumer.accept(options);
				return options;
			};
		}
	}


	/**
	 * {@link BatchLoaderWithContext} that delegates to a {@link Flux} batch
	 * loading function and exposes Reactor context to it.
	 */
	private static class ReactorBatchLoader<K, V> implements BatchLoaderWithContext<K, V> {

		private final String name;

		private final BiFunction<List<K>, BatchLoaderEnvironment, Flux<V>> loader;

		private final Supplier<DataLoaderOptions> optionsSupplier;

		private ReactorBatchLoader(String name,
				BiFunction<List<K>, BatchLoaderEnvironment, Flux<V>> loader,
				Supplier<DataLoaderOptions> optionsSupplier) {

			this.name = name;
			this.loader = loader;
			this.optionsSupplier = optionsSupplier;
		}

		public String getName() {
			return this.name;
		}

		public DataLoaderOptions getOptions() {
			return this.optionsSupplier.get();
		}

		@Override
		@SuppressWarnings("deprecation")
		public CompletionStage<List<V>> load(List<K> keys, BatchLoaderEnvironment environment) {
			GraphQLContext graphQLContext = environment.getContext();
			ContextSnapshot snapshot = ContextSnapshot.captureFrom(graphQLContext);
			try {
				return snapshot.wrap(() ->
								this.loader.apply(keys, environment)
										.collectList()
										.contextWrite(snapshot::updateContext)
										.toFuture())
						.call();
			}
			catch (Exception ex) {
				return CompletableFuture.failedFuture(ex);
			}
		}
	}


	/**
	 * {@link MappedBatchLoaderWithContext} that delegates to a {@link Mono}
	 * batch loading function and exposes Reactor context to it.
	 */
	private static class ReactorMappedBatchLoader<K, V> implements MappedBatchLoaderWithContext<K, V> {

		private final String name;

		private final BiFunction<Set<K>, BatchLoaderEnvironment, Mono<Map<K, V>>> loader;

		private final Supplier<DataLoaderOptions> optionsSupplier;

		private ReactorMappedBatchLoader(String name,
				BiFunction<Set<K>, BatchLoaderEnvironment, Mono<Map<K, V>>> loader,
				Supplier<DataLoaderOptions> optionsSupplier) {

			this.name = name;
			this.loader = loader;
			this.optionsSupplier = optionsSupplier;
		}

		public String getName() {
			return this.name;
		}

		public DataLoaderOptions getOptions() {
			return this.optionsSupplier.get();
		}

		@Override
		@SuppressWarnings("deprecation")
		public CompletionStage<Map<K, V>> load(Set<K> keys, BatchLoaderEnvironment environment) {
			GraphQLContext graphQLContext = environment.getContext();
			ContextSnapshot snapshot = ContextSnapshot.captureFrom(graphQLContext);
			try {
				return snapshot.wrap(() ->
								this.loader.apply(keys, environment)
										.contextWrite(snapshot::updateContext)
										.toFuture())
						.call();
			}
			catch (Exception ex) {
				return CompletableFuture.failedFuture(ex);
			}
		}

	}

}
