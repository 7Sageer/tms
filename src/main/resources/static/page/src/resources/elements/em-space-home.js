import { bindable, containerless } from 'aurelia-framework';

@containerless
export class EmSpaceHome {

    @bindable value;

    valueChanged(newValue, oldValue) {

    }

    activate(params, routeConfig, navigationInstruction) {
        this.sid = params.sid;

        routeConfig.navModel.setTitle(`${this.sid ? this.sid + ' | ' : ''}TMS`);

    }
}
