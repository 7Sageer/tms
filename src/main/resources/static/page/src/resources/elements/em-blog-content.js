import {
    bindable,
    containerless
} from 'aurelia-framework';
import {
    default as Clipboard
} from 'clipboard';
import {
    default as clipboard
} from 'clipboard-js';

@containerless
export class EmBlogContent {

    @bindable blogInfo;

    blogInfoChanged(newValue, oldValue) {

        if (!this.blogInfo) return;

        _.defer(() => {
            ea.publish(nsCons.EVENT_LANDING_BLOG_DIR, {
                mkRef: this.mkRef
            });
        });

    }

    attached() {
        new Clipboard('.em-blog-content .tms-clipboard')
            .on('success', function (e) {
                toastr.success('复制到剪贴板成功!');
            }).on('error', function (e) {
                toastr.error('复制到剪贴板失败!');
            });

        this.codeClHandler = function (event) {
            if (event.ctrlKey) {
                event.stopImmediatePropagation();
                event.preventDefault();
                clipboard.copy($(event.currentTarget).attr('data-code')).then(
                    () => {
                        toastr.success('复制到剪贴板成功!');
                    },
                    (err) => {
                        toastr.error('复制到剪贴板失败!');
                    }
                );
            }
        };

        this.preCodeClHandler = function (event) {
            if (event.ctrlKey) {
                event.stopImmediatePropagation();
                event.preventDefault();
                clipboard.copy($(event.currentTarget).find('i[data-clipboard-text]').attr('data-clipboard-text')).then(
                    () => {
                        toastr.success('复制到剪贴板成功!');
                    },
                    (err) => {
                        toastr.error('复制到剪贴板失败!');
                    }
                );
            }
        };

        this.dimmerClHandler = (event) => {
            if ($(event.target).is('.ppt-dimmer')) {
                $('.ppt-dimmer').dimmer('hide');
            }
        };

        this.fileDownloadLinkClickHandler = (event) => {

            if (this.blogInfo.blog.fileReadonly) {
                toastr.error('附件文件下载权限不足！');
                event.preventDefault();
            }
        };

        $('.em-blog-content').on('click', 'code[data-code]', this.codeClHandler);

        $('.em-blog-content').on('click', '.pre-code-wrapper', this.preCodeClHandler);

        $('.ppt-dimmer').on('click', this.dimmerClHandler);

        // file online preview
        $('.em-blog-content').on('click', '.tms-blog-content.markdown-body a[href*="admin/file/download/"],a.tms-file-download-item', this.fileDownloadLinkClickHandler);
    }

    detached() {
        $('.em-blog-content').off('click', 'code[data-code]', this.codeClHandler);
        $('.em-blog-content').off('click', '.pre-code-wrapper', this.preCodeClHandler);
        $('.ppt-dimmer').off('click', this.dimmerClHandler);
        // file online preview
        $('.em-blog-content').off('click', '.tms-blog-content.markdown-body a[href*="admin/file/download/"],a.tms-file-download-item', this.fileDownloadLinkClickHandler);

    }

    pptViewHandler() {

        $('.ppt-dimmer').dimmer('show');

        ea.publish(nsCons.EVENT_PPT_VIEW_CLICK, {});
    }
}
