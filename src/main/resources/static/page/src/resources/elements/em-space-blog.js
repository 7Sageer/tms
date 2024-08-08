import { bindable, containerless } from 'aurelia-framework';

@containerless
export class EmSpaceBlog {

    /**
     * 构造函数
     */
    constructor() {
        // this.subscribe = ea.subscribe(nsCons.EVENT_BLOG_GOT, (payload) => {
        //     this.routeConfig && this.routeConfig.navModel.setTitle(`${payload.title + ' | '}${this.sid ? this.sid + ' | ' : ''}TMS`);
        // });
    }

    activate(params, routeConfig, navigationInstruction) {
        this.sid = params.sid;
        this.id = params.id;
        this.routeConfig = routeConfig;

        routeConfig.navModel.setTitle(`${this.sid ? this.sid + ' | ' : ''}TMS`);

        return $.get(`/free/space/home/blog/${this.id}`, (data) => {
            if (!data.success) {
                toastr.error(data.data);
            } else {
                this.blogInfo = data.data;
                this.routeConfig.navModel.setTitle(`${this.blogInfo.blog.title + ' | '}${this.sid ? this.sid + ' | ' : ''}TMS`);
            }
        });
    }
}
