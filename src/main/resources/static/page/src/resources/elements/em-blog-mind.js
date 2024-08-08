import {
    bindable,
    containerless
} from 'aurelia-framework';

@containerless
export class EmBlogMind {

    @bindable blog;
    @bindable comment;

    baseRes = utils.getResourceBase();

    blogChanged(newValue, oldValue) {
        // newValue && this.initBlog(newValue);
    }

    constructor() {
        this.subscribe = ea.subscribe(nsCons.EVENT_PPT_VIEW_CLICK, (payload) => {
            this.initBlog();
        });
    }

    initBlog() {
        _.defer(() => {
            if (!this.blog) return;
            if (this.blog.editor == 'Mind') {
                let shareId = this.blog.shareId ? this.blog.shareId : '';
                $(`.em-blog-mind[data-id="${this.blog.id}"] > iframe`).attr('src', `${this.baseRes}page/mind.html?id=${this.blog.id}&shareId=${shareId}&readonly&free&_=${new Date().getTime()}`);
            }
        });
    }

    initComment() {
        _.defer(() => {
            if (!this.comment) return;
            if (this.comment.editor == 'Mind') {
                $(`.em-blog-mind[data-cid="${this.comment.id}"] > iframe`).attr('src', `${this.baseRes}page/mind.html?comment&cid=${this.comment.id}&readonly&free&_=${new Date().getTime()}`);
            }
        });
    }

    attached() {
        this.initBlog();
        this.initComment();
    }

    /**
     * 当数据绑定引擎从视图解除绑定时被调用
     */
    unbind() {
        this.subscribe.dispose();
    }
}
