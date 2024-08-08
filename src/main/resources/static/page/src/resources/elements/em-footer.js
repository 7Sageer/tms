import { bindable, containerless } from 'aurelia-framework';

@containerless
export class EmFooter {

    attached() {
        $(this.logoRef).on('mouseenter', (event) => {
            $(this.logoRef).animateCss('flip');
        });
    }
}
