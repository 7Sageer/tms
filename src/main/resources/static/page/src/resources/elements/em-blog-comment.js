import { bindable, containerless } from 'aurelia-framework';

@containerless
export class EmBlogComment {

    @bindable id;

    @bindable shareId;

    @bindable shareBlogId;

    idChanged(newValue, oldValue) {
        this._getCommentsById();
    }

    shareIdChanged(newValue, oldValue) {
        this._getCommentsByShareId();
    }

    /**
     * 当视图被附加到DOM中时被调用
     */
    attached() {
        // this._getCommentsById();
    }

    _getCommentsById() {

        if (!this.id) {
            return;
        }

        let url = `/free/home/blog/${this.id}/comments`;

        $.get(url, {
            page: 0,
            size: 1000
        }, (data) => {
            if (data.success) {
                this.comments = data.data.content;
            } else {
                toastr.error(data.data);
            }
        });
    }

    _getCommentsByShareId() {

        if (!this.shareId) {
            return;
        }

        let url = `/free/blog/share/${this.shareId}/comments`;

        $.get(url, {
            page: 0,
            size: 1000
        }, (data) => {
            if (data.success) {
                this.comments = data.data.content;
            } else {
                toastr.error(data.data);
            }
        });
    }
}
