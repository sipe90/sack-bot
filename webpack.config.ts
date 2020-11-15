import { CleanWebpackPlugin } from 'clean-webpack-plugin'
import HtmlWebpackPlugin from 'html-webpack-plugin'
import path from 'path'
// @ts-ignore
import { getThemeVariables } from 'antd/dist/theme'
import tsImportPluginFactory from 'ts-import-plugin'
import { Configuration, DefinePlugin } from 'webpack'

const SRC_ROOT = path.resolve(__dirname, 'src', 'main', 'webapp')
const DEST = path.resolve(__dirname, 'build', 'webapp')

const config: Configuration = {
    entry: {
        main: path.resolve(SRC_ROOT, 'index.tsx')
    },
    output: {
        path: DEST,
        filename: '[name].[hash].bundle.js',
        publicPath: '/'
    },
    module: {
        rules: [
            {
                test: /\.tsx?$/,
                exclude: /node_modules/,
                loader: 'ts-loader',
                options: {
                    transpileOnly: true,
                    getCustomTransformers: () => ({
                        before: [tsImportPluginFactory({ libraryName: 'antd', style: true, libraryDirectory: 'lib' })]
                    }),
                    compilerOptions: {
                        module: 'es2015'
                    }
                }
            },
            {
                test: /\.css$/,
                use: ['style-loader', 'css-loader']
            },
            {
                test: /\.less$/,
                // include: /node_modules\/antd\/lib/,
                use: [{
                    loader: 'style-loader',
                }, {
                    loader: 'css-loader',
                }, {
                    loader: 'less-loader',
                    options: {
                        lessOptions: {
                            modifyVars: getThemeVariables({
                                dark: false,
                                compact: true
                            }),
                            javascriptEnabled: true
                        },
                    },
                }]
            },
            {
                test: /\.(png|svg|jpg|jpeg|gif)$/i,
                loader: 'file-loader',
            }
        ]
    },
    devServer: {
        port: 3000,
        open: true,
        useLocalIp: false,
        historyApiFallback: true,
        proxy: [{
            context: ['/oauth2', '/login/oauth2', '/logout', '/api'],
            target: 'http://localhost:8080'
        }],
    },
    plugins: [
        new CleanWebpackPlugin(),
        new HtmlWebpackPlugin({
            template: path.resolve(SRC_ROOT, 'public', 'index.html'),
            favicon: path.resolve(SRC_ROOT, 'public', 'favicon.ico')
        }),
        new DefinePlugin({
            VERSION: JSON.stringify(process.env.npm_package_version)
        })
    ],
    resolve: {
        alias: {
            '@': path.resolve(__dirname, 'src', 'main', 'webapp')
        },
        extensions: ['.tsx', '.ts', '.js'],
        modules: ['node_modules']
    },
    optimization: {
        splitChunks: {
            chunks: 'all'
        },
        runtimeChunk: true
    }
}

export default config
