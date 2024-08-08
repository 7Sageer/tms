import { bindable, containerless } from 'aurelia-framework';

@containerless
export class EmBlogShare {

    activate(params, routeConfig, navigationInstruction) {

        this.shareId = params.id;

        return $.get(`/free/blog/share/${this.shareId}`, (data) => {
            if (!data.success) {
                toastr.error(data.data);
            } else {
                this.blog = data.data;
                this.blogInfo = { blog: this.blog };
                routeConfig.navModel.setTitle(`${this.blog.title + ' | '}TMS`);
            }
        });

    }
}
