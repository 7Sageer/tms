import 'tms-semantic-ui';
import 'semantic-ui-calendar';
import 'jquery-format';
import { Base64 } from 'js-base64';

export class App {

    constructor() {
        this.init();
        this.initCalendar();

        this.subscribe = ea.subscribe(nsCons.EVENT_APP_ROUTER_NAVIGATE, (payload) => {
            this.router && this.router.navigate(`${payload.to}`);
        });
    }

    attached() {
        // file online preview
        $('body').on('mouseenter', '.markdown-body a[href*="admin/file/download/"]', (event) => {
            event.preventDefault();

            if (!window.tmsSysConfig || !window.tmsSysConfig.fileViewUrl) return;

            let $a = $(event.currentTarget);

            if ($a.children('.tms-file-online-view-wrapper').length === 0) {
                $a.append(`<span class="tms-file-online-view-wrapper"><i class="unhide large black link icon" title="在线文件预览"></i></span>`);
            }

        });

        $('body').on('click', '.tms-file-online-view-wrapper i.unhide.icon', event => {
            event.preventDefault();

            if (!window.tmsSysConfig || !window.tmsSysConfig.fileViewUrl) return;

            let $item = $(event.currentTarget);

            let $a = $item.closest('a');

            if ($a.length === 0) return;

            let url = `${$a.attr('href')}?onlinepreview=1&fullfilename=${$a.text()}`;

            console.log('window.tmsSysConfig:', window.tmsSysConfig);

            // 3.x.x 及以上版本x需要对预览文件url进行base64编码
            // http://kkfileview.keking.cn/zh-cn/docs/production.html
            if (window.tmsSysConfig && window.tmsSysConfig.fileViewEncodePreviewUrl) {
                url = Base64.encode(url);
            }

            window.open(`${window.tmsSysConfig.fileViewUrl}/onlinePreview?url=` + encodeURIComponent(url));
        });

    }

    /**
     * 当数据绑定引擎从视图解除绑定时被调用
     */
    unbind() {
        this.subscribe.dispose();
    }

    init() {

        $.fn.dropdown.settings.forceSelection = false;

        // ui form 验证提示信息国际化
        _.extend($.fn.form.settings.prompt, {
            empty: '{name}不能为空',
            checked: '{name}必须被勾选',
            email: '{name}必须是正确的邮件格式',
            url: '{name}必须是正确的URL格式',
            regExp: '{name}验证格式不正确',
            integer: '{name}必须为一个整数',
            decimal: '{name}必须为一个小数',
            number: '{name}必须设置为一个数字',
            is: '{name}必须符合规则"{ruleValue}"',
            isExactly: '{name}必须精确匹配"{ruleValue}"',
            not: '{name}不能设置为"{ruleValue}"',
            notExactly: '{name}不能准确设置为"{ruleValue}"',
            contain: '{name}需要包含"{ruleValue}"',
            containExactly: '{name}需要精确包含"{ruleValue}"',
            doesntContain: '{name}不能包含"{ruleValue}"',
            doesntContainExactly: '{name}不能精确包含"{ruleValue}"',
            minLength: '{name}必须至少包含{ruleValue}个字符',
            length: '{name}必须为{ruleValue}个字符',
            exactLength: '{name}必须为{ruleValue}个字符',
            maxLength: '{name}必须不能超过{ruleValue}个字符',
            match: '{name}必须匹配{ruleValue}字段',
            different: '{name}必须不同于{ruleValue}字段',
            creditCard: '{name}必须是一个正确的信用卡数字格式',
            minCount: '{name}必须至少包含{ruleValue}个选择项',
            exactCount: '{name}必须准确包含{ruleValue}个选择项',
            maxCount: '{name} 必须有{ruleValue}或者更少个选择项'
        });
    }

    initCalendar() {
        $.fn.calendar.settings.text = {
            days: ['日', '一', '二', '三', '四', '五', '六'],
            months: ['1月', '2月', '3月', '4月', '5月', '6月', '7月', '8月', '9月', '10月', '11月', '12月'],
            monthsShort: ['1月', '2月', '3月', '4月', '5月', '6月', '7月', '8月', '9月', '10月', '11月', '12月'],
            today: '今天',
            now: '现在',
            am: '上午',
            pm: '下午'
        };

        $.fn.calendar.settings.formatter.date = function(date, settings) {
            if (!date) return '';
            var day = date.getDate();
            var month = date.getMonth() + 1;
            var year = date.getFullYear();
            return $.format.date(date, 'yyyy-MM-dd');
        };

        return this;
    }

    /**
     * 配置路由
     * @param  {[object]} config 路由配置
     * @param  {[object]} router 路由
     */
    configureRouter(config, router) {

        config.map([{
            route: ['home'],
            name: 'home',
            moduleId: 'resources/elements/em-home',
            nav: false,
            title: 'TMS'
        }, {
            route: [':sid/home'],
            name: 'SpaceHome',
            moduleId: 'resources/elements/em-space-home',
            nav: false,
            title: 'TMS'
        }, {
            route: ['blog/:id'],
            name: 'blog',
            moduleId: 'resources/elements/em-blog',
            nav: false,
            title: 'TMS'
        }, {
            route: ['blog/share/:id'],
            name: 'blog',
            moduleId: 'resources/elements/em-blog-share',
            nav: false,
            title: 'TMS'
        }, {
            route: [':sid/blog/:id'],
            name: 'spaceBlog',
            moduleId: 'resources/elements/em-space-blog',
            nav: false,
            title: 'TMS'
        }, {
            route: '',
            redirect: `home`
        }]);

        this.router = router;

    }

    /**
     * 在视图模型(ViewModel)展示前执行一些自定义代码逻辑
     * @param  {[object]} params                参数
     * @param  {[object]} routeConfig           路由配置
     * @param  {[object]} navigationInstruction 导航指令
     * @return {[promise]}                      你可以可选的返回一个延迟许诺(promise), 告诉路由等待执行bind和attach视图(view), 直到你完成你的处理工作.
     */
    activate(params, routeConfig, navigationInstruction) {

    }
}
