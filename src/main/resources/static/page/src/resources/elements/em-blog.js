import { bindable, containerless } from 'aurelia-framework';

@containerless
export class EmBlog {

    activate(params, routeConfig, navigationInstruction) {
        this.id = params.id;

        return $.get(`/free/home/blog/${this.id}`, (data) => {
            if (!data.success) {
                toastr.error(data.data);
            } else {
                this.blogInfo = data.data;
                routeConfig.navModel.setTitle(`${this.blogInfo.blog.title + ' | '}TMS`);
            }
        });
    }
}
