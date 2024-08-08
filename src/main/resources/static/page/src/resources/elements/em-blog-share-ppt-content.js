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
export class EmBlogSharePptContent {

    @bindable blog;

    attached() {
        new Clipboard('.em-blog-share-ppt-content .tms-clipboard')
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

        this.fileDownloadLinkClickHandler = (event) => {

            if (this.blog.fileReadonly) {
                toastr.error('附件文件下载权限不足！');
                event.preventDefault();
            }
        };

        $('.em-blog-share-ppt-content').on('click', 'code[data-code]', this.codeClHandler);

        $('.em-blog-share-ppt-content').on('click', '.pre-code-wrapper', this.preCodeClHandler);

        // file online preview
        $('.em-blog-share-ppt-content').on('click', '.tms-blog-content.markdown-body a[href*="admin/file/download/"],a.tms-file-download-item', this.fileDownloadLinkClickHandler);

        if (_.includes(wurl(), '?read')) { // read
            $('.ppt-dimmer').dimmer('show');
        }
    }

    detached() {

        $('.em-blog-share-ppt-content').off('click', 'code[data-code]', this.codeClHandler);
        $('.em-blog-share-ppt-content').off('click', '.pre-code-wrapper', this.preCodeClHandler);
        // file online preview
        $('.em-blog-share-ppt-content').off('click', '.tms-blog-content.markdown-body a[href*="admin/file/download/"],a.tms-file-download-item', this.fileDownloadLinkClickHandler);

    }

}
