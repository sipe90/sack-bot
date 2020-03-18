import { CleanWebpackPlugin } from 'clean-webpack-plugin'
import HtmlWebpackPlugin from 'html-webpack-plugin'
import path from 'path'
import { Configuration, DefinePlugin } from 'webpack'

const SRC_ROOT = path.resolve(__dirname, 'src', 'main', 'webapp')
const DEST = path.resolve(__dirname, 'build', 'resources', 'main', 'static')

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
                test: /\.(png|woff|woff2|eot|ttf|svg)$/,
                loader: 'url-loader?limit=100000'
            }
        ]
    },
    devServer: {
        port: 3000,
        open: true,
        useLocalIp: true,
        historyApiFallback: true,
        proxy: {
            '/api': 'http://localhost:8080'
        }
    },
    plugins: [
        new CleanWebpackPlugin(),
        new HtmlWebpackPlugin({
            template: path.resolve(SRC_ROOT, 'index.html'),
            favicon: path.resolve(SRC_ROOT, 'favicon.ico')
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
