import {
    bindable,
    containerless
} from 'aurelia-framework';

@containerless
export class EmBlogList {

    @bindable sid;

    @bindable id;

    search = '';

    page = 0;
    size = 15;
    blogs = [];
    blogTree = [];
    blogSize = 0;
    topBlogs = [];

    constructor() {
        this.doSearch = _.debounce(() => {
            this.blogs = [];
            this.topBlogs = [];
            this.blogTree = [];
            this.page = 0;
            this._listBlogs();
        }, 500);
    }

    attached() {
        $(this.ddSearchRef).dropdown({
            onChange: (value, text, $choice) => {
                this.doSearch();
                $(this.searchInputRef).focus();
            }
        });
    }

    _listBlogs() {

        let prefix = $(this.ddSearchRef).dropdown("get value");

        this.size = 20;
        let url = `/free/home/blog/page/search`;
        if (this.sid) {
            this.size = 10000;
            url = `/free/space/home/${this.sid}/blog/page/search`;
        }

        this.ajax = $.get(url, {
            search: (this.search ? prefix : '') + this.search,
            size: this.size,
            page: this.page
        }, (data) => {
            this.blogPage = data.data;
            this.blogSize = data.data.content.length;
            let pidBlogs = [];
            let noPidBlogs = [];

            if (this.search) {
                this.topBlogs.push(...data.data.content);
                return;
            }

            if (this.sid) {
                _.each(data.data.content, blog => {
                    if (!blog.pid) {
                        noPidBlogs.push(blog);
                        if (blog.dir) {
                            let dir = _.find(this.blogTree, {
                                id: blog.dir.id
                            });
                            if (dir) {
                                dir.blogs.push(blog);
                            } else {
                                blog.dir.blogs = [blog];
                                blog.dir._open = true;
                                this.blogTree.push(blog.dir);
                            }
                        } else {
                            this.topBlogs.push(blog);
                        }
                    } else {
                        pidBlogs.push(blog);
                    }
                });
                this._treeBlogs(noPidBlogs, pidBlogs);
            } else {
                _.each(data.data.content, blog => {
                    this.blogs.push(blog);
                    if (!blog.pid) {
                        this.topBlogs.push(blog);
                    }
                });
                this._treeBlogs(this.topBlogs, this.blogs);
            }
        });
    }

    _treeBlogs(blogs, _blogs) {
        _.each(blogs, blog => {
            if (blog.hasChild) {
                let childs = _.filter(_blogs, {
                    pid: blog.id
                });
                if (childs.length > 0) {
                    blog._open = true;
                    blog._childs = childs;
                    this._treeBlogs(blog._childs, _blogs);
                }
            }
        });
    }

    moreHandler() {
        this.page++;
        this._listBlogs();
    }

    bind(bindingCtx, overrideCtx) {
        this._listBlogs();
    }

    keyupHandler(event) {
        this.doSearch();
    }

    searchFocusHandler() {
        $(this.searchRemoveRef).show();
    }

    searchBlurHandler() {
        if (!this.search) {
            $(this.searchRemoveRef).hide();
        }
    }

    clearSearchHandler() {
        this.search = '';
        $(this.searchInputRef).focus();
        this.doSearch();
    }

    loadChildBlogs(blog) {
        if (!blog.hasChild) return;

        blog._open = !blog._open;
        if (blog._open && !blog._childs) {
            $.get('/free/home/blog/list/by/pid', {
                id: blog.id
            }, (data) => {
                if (data.success) {
                    blog._childs = data.data;
                    blog.hasChild = blog._childs.length > 0;
                } else {
                    toastr.error(data.data);
                }
            });
        }
    }

    dirToggleHandler(dir) {
        dir._open = !dir._open;
    }
}
