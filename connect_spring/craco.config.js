// craco.config.js
const webpack = require('webpack');

module.exports = {
    webpack: {
        configure: (config) => {
            // node: 스킴을 브라우저 모듈로 매핑
            config.resolve = config.resolve || {};
            config.resolve.alias = {
                ...(config.resolve.alias || {}),
                'node:url': 'url',                 // ← 오류의 직접 원인 처리
                'node:path': 'path-browserify',
                'node:stream': 'stream-browserify',
                'node:buffer': 'buffer',
            };

            // Webpack 5 Node 코어 대체
            config.resolve.fallback = {
                ...(config.resolve.fallback || {}),
                url: require.resolve('url/'),
                path: require.resolve('path-browserify'),
                stream: require.resolve('stream-browserify'),
                buffer: require.resolve('buffer/'),
            };

            // 전역 process/Buffer 주입(일부 패키지 호환)
            config.plugins = [
                ...(config.plugins || []),
                new webpack.ProvidePlugin({
                    process: 'process/browser',
                    Buffer: ['buffer', 'Buffer'],
                }),
            ];

            return config;
        },
    },
};