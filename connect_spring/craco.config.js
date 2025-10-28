// craco.config.js
const webpack = require('webpack');
const NodePolyfillPlugin = require('node-polyfill-webpack-plugin');

module.exports = {
    webpack: {
        configure: (config) => {
            // 1) node: 프리픽스 제거(가장 먼저 적용되어야 함)
            config.plugins = [
                new webpack.NormalModuleReplacementPlugin(/^node:(.+)$/, (resource) => {
                    // 예: node:url -> url
                    resource.request = resource.request.replace(/^node:/, '');
                }),

                // 2) Node 코어 폴리필 자동 주입
                new NodePolyfillPlugin({
                    // 필요시 포함할 별칭만 지정 가능(지금은 기본값으로 충분)
                    // includeAliases: ['buffer','process','path','url','stream','util','crypto'],
                }),

                // 3) 전역 process/Buffer 주입 (일부 라이브러리 호환)
                new webpack.ProvidePlugin({
                    process: require.resolve('process/browser.js'), // 확장자 포함!
                    Buffer: ['buffer', 'Buffer'],
                }),

                ...(config.plugins || []),
            ];

            // 4) alias(보조 안전망)
            config.resolve = config.resolve || {};
            config.resolve.alias = {
                ...(config.resolve.alias || {}),
                'node:url': 'url',
                'node:path': 'path-browserify',
                'node:stream': 'stream-browserify',
                'node:buffer': 'buffer',
                // 필요하면: 'node:util': 'util', 'node:crypto': 'crypto-browserify'
            };

            // 5) fallback(보조 안전망)
            config.resolve.fallback = {
                ...(config.resolve.fallback || {}),
                url: require.resolve('url/'),
                path: require.resolve('path-browserify'),
                stream: require.resolve('stream-browserify'),
                buffer: require.resolve('buffer/'),
                process: require.resolve('process/browser.js'),
                // 필요하면: util: require.resolve('util/'), crypto: require.resolve('crypto-browserify'),
            };

            return config;
        },
    },
};
