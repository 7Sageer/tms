import { bindable, containerless } from 'aurelia-framework';

@containerless
export class EmBlogDir {

    mkRef;
    dirHtml = '';

    /**
     * 构造函数
     */
    constructor() {
        this.subscribe = ea.subscribe(nsCons.EVENT_LANDING_BLOG_DIR, (payload) => {
            _.defer(() => {
                this.mkRef = payload.mkRef;
                this._dir();
            });
        });
    }

    unbind() {
        this.subscribe.dispose();
    }

    attached() {
        $('.em-blog-dir').on('click', '.wiki-dir-item', (event) => {
            event.preventDefault();
            $('body').scrollTo(`#${$(event.currentTarget).attr('data-id')}`, 200, {
                offset: 0
            });
        });
    }

    _dir() {
        this.dir = utils.dir($(this.mkRef), 'tms-blog-dir-item-');
        if (this.dir) {
            this.dirHtml = this.dir.wrap('<div/>').parent().html();
        }

    }
}
