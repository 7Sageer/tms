import gulp from 'gulp';
import browserSync from 'browser-sync'
import historyApiFallback from 'connect-history-api-fallback/lib';;
import project from '../aurelia.json';
import build from './build';
import { CLIOptions } from 'aurelia-cli';
// https://github.com/chimurai/http-proxy-middleware#http-proxy-middleware-options
import {
    default as proxyMiddleware
} from 'http-proxy-middleware';

function onChange(path) {
    console.log(`File Changed: ${path}`);
}

function reload(done) {
    browserSync.reload();
    done();
}

let serve = gulp.series(
    build,
    done => {
        browserSync({
            online: false,
            open: false,
            port: 9000,
            logLevel: 'silent',
            server: {
                baseDir: ['.'],
                middleware: [historyApiFallback(), function(req, res, next) {
                        res.setHeader('Access-Control-Allow-Origin', '*');
                        next();
                    },
                    proxyMiddleware(['/free', '/admin', '/upload', '/lib'], {
                        target: 'http://localhost',
                        changeOrigin: true
                    }),
                ]
            }
        }, function(err, bs) {
            let urls = bs.options.get('urls').toJS();
            console.log(`Application Available At: ${urls.local}`);
            console.log(`BrowserSync Available At: ${urls.ui}`);
            done();
        });
    }
);

let refresh = gulp.series(
    build,
    reload
);

let watch = function() {
    gulp.watch(project.transpiler.source, refresh).on('change', onChange);
    gulp.watch(project.markupProcessor.source, refresh).on('change', onChange);
    gulp.watch(project.cssProcessor.source, refresh).on('change', onChange)
}

let run;

if (CLIOptions.hasFlag('watch')) {
    run = gulp.series(
        serve,
        watch
    );
} else {
    run = serve;
}

export default run;
