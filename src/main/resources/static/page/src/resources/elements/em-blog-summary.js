import {
    bindable,
    containerless
} from 'aurelia-framework';

@containerless
export class EmBlogSummary {

    @bindable sid;

    page = 0;
    size = 7;
    blogs = [];
    loading = false;
    maxLoad = 3; //最大滚动加载次数

    constructor() {

        let tmpThis = this; //下文因function this对象变化

        this.winScrollHandler = function () {
            let scrollHeight = $(document).height(); //document的滚动高度
            let scrollTop = $(document).scrollTop() || $(this).scrollTop(); //已滚动高度
            let windowHeight = $(this).height(); //可视区高度
            let overHeight = 200; //距底部开始加载的距离
            // 简单判断，避免滚动过快时多次加载
            // 理论上应该是页面渲染完再修改，aurelia无此回调接口，观察者属性亦不生效，退而求其次只判断数据加载
            if (!tmpThis.loading && scrollTop + windowHeight + overHeight >= scrollHeight && tmpThis.page < tmpThis.maxLoad) {
                tmpThis.page++;
                tmpThis._getBlogs();
            }
        };

        $(window).on('scroll', this.winScrollHandler); // 滚动加载

    }

    attached() {

        this.fileDownloadLinkClickHandler = (event) => {

            let $item = $(event.currentTarget).closest('.tms-blog-item');

            let blog = _.find(this.blogs, {
                id: +$item.attr('data-id')
            });

            if (blog && blog.fileReadonly) {
                toastr.error('附件文件下载权限不足！');
                event.preventDefault();
            }
        };

        // file online preview
        $('.em-blog-summary').on('click', '.tms-blog-content.markdown-body a[href*="admin/file/download/"],a.tms-file-download-item', this.fileDownloadLinkClickHandler);

    }

    bind() {
        this._getBlogs();
    }

    detached() {
        // file online preview
        $('.em-blog').off('click', '.tms-blog-content.markdown-body a[href*="admin/file/download/"],a.tms-file-download-item', this.fileDownloadLinkClickHandler);

    }

    /**
     * 当数据绑定引擎从视图解除绑定时被调用
     */
    unbind() {
        $(window).off('scroll', this.winScrollHandler); // 滚动加载

    }

    _getBlogs() {
        let url = `/free/home/blogs`;
        if (this.sid) {
            url = `/free/space/home/${this.sid}/blogs`;
        }
        this.loading = true;
        this.ajax = $.get(url, {
            page: this.page,
            size: this.size,
        }, (data) => {
            this.blogPage = data.data;
            this.blogs.push(...data.data.content);
            this.loading = false;
        });
    }

    moreHandler() {
        this.page++;
        this._getBlogs();
    }
}
