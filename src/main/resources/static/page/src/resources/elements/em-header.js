import { bindable, containerless } from 'aurelia-framework';

@containerless
export class EmHeader {

    @bindable sid;

    user = null;

    bind() {
        $.get('/admin/user/loginUser', (data) => {
            if (data.success) {
                this.user = data.data;
                nsCtx.loginUser = this.user;
                nsCtx.isSuper = utils.isSuperUser(this.user);
                nsCtx.isAdmin = utils.isAdminUser(this.user);
            }
        }).always(() => {
            // this.user = { name: '张三' };
        });
    }

    attached() {
        $(this.logoRef).on('mouseenter', (event) => {
            $(this.logoRef).animateCss('flip');
        });
    }

    logoutHandler() {
        $.post('/admin/logout').always(() => {
            this.user = null;
            nsCtx.loginUser = {};
            nsCtx.isSuper = false;
            nsCtx.isAdmin = false;
            // window.location.href = `/`;
        });
    }
}
