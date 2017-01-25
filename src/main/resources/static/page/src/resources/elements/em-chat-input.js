import { bindable, containerless, inject } from 'aurelia-framework';
import 'textcomplete';
import tips from 'common/common-tips';
import {
    default as SimpleMDE
} from 'simplemde';

@containerless
export class EmChatInput {

    @bindable chatTo;
    @bindable isAt;
    @bindable channel;
    members = [];

    channelChanged() {

        if (this.channel) {
            this.members = [nsCtx.memberAll, ...this.channel.members];
        } else {
            this.members = [];
        }

    }

    /**
     * 构造函数
     */
    constructor() {
        this.subscribe = ea.subscribe(nsCons.EVENT_SHOW_HOTKEYS_MODAL, (payload) => {
            this.emHotkeysModal.show();
        });
        this.subscribe1 = ea.subscribe(nsCons.EVENT_CHAT_CHANNEL_MEMBER_ADD_OR_REMOVE, (payload) => {
            this.members = [nsCtx.memberAll, ...payload.members];
        });
        this.subscribe2 = ea.subscribe(nsCons.EVENT_CHAT_MSG_INSERT, (payload) => {
            this.insertContent(payload.content);
        });
    }

    /**
     * 当数据绑定引擎从视图解除绑定时被调用
     */
    unbind() {
        this.subscribe.dispose();
        this.subscribe1.dispose();
        this.subscribe2.dispose();
    }

    initHotkeys() {
        $(document).bind('keydown', 'ctrl+i', () => {
            event.preventDefault();
            this.simplemde.codemirror.focus();
        });
    }

    /**
     * 当视图被附加到DOM中时被调用
     */
    attached() {
        this.initSimpleMDE(this.chatInputRef);
        this.initDropzone();
        this.initPaste();
        this.initHotkeys();
    }

    initPaste() {
        $(this.$chatMsgInputRef).pastableTextarea().on('pasteImage', (ev, data) => {

            $.post('/admin/file/base64', {
                dataURL: data.dataURL,
                type: data.blob.type
            }, (data, textStatus, xhr) => {
                if (data.success) {
                    this.insertContent('![{name}]({baseURL}{path}{uuidName})'
                        .replace(/\{name\}/g, data.data.name)
                        .replace(/\{baseURL\}/g, utils.getBaseUrl() + '/')
                        .replace(/\{path\}/g, data.data.path)
                        .replace(/\{uuidName\}/g, data.data.uuidName));
                }
            });
        }).on('pasteImageError', (ev, data) => {
            toastr.error(data.message, '剪贴板粘贴图片错误!');
        });
    }

    initDropzone() {
        this.initUploadDropzone($('.CodeMirror-wrap', this.inputRef), () => {
            return this.$chatMsgInputRef
        }, false);
        this.initUploadDropzone($(this.btnItemUploadRef).children().andSelf(), () => {
            return this.$chatMsgInputRef
        }, true);

        $(this.chatBtnRef).popup({
            inline: true,
            hoverable: true,
            position: 'bottom left',
            delay: {
                show: 300,
                hide: 300
            }
        });

    }

    initUploadDropzone(domRef, getInputTargetCb, clickable) {

        let _this = this;

        $(domRef).dropzone({
            url: "/admin/file/upload",
            paramName: 'file',
            clickable: !!clickable,
            dictDefaultMessage: '',
            maxFilesize: 10,
            addRemoveLinks: true,
            previewsContainer: this.chatStatusBarRef,
            previewTemplate: this.previewTemplateRef.innerHTML,
            dictCancelUpload: '取消上传',
            dictCancelUploadConfirmation: '确定要取消上传吗?',
            dictFileTooBig: '文件过大({{filesize}}M),最大限制:{{maxFilesize}}M',
            init: function() {
                this.on("sending", function(file, xhr, formData) {
                    if (!getInputTargetCb()) {
                        this.removeAllFiles(true);
                    }
                });
                this.on("success", function(file, data) {
                    if (data.success) {

                        $.each(data.data, function(index, item) {
                            if (item.type == 'Image') {
                                _this.insertContent('![{name}]({baseURL}{path}{uuidName}) '
                                    .replace(/\{name\}/g, item.name)
                                    .replace(/\{baseURL\}/g, utils.getBaseUrl() + '/')
                                    .replace(/\{path\}/g, item.path)
                                    .replace(/\{uuidName\}/g, item.uuidName));
                            } else {
                                _this.insertContent('[{name}]({baseURL}{path}{uuidName}) '
                                    .replace(/\{name\}/g, item.name)
                                    .replace(/\{baseURL\}/g, utils.getBaseUrl() + '/')
                                    .replace(/\{path\}/g, "admin/file/download/")
                                    .replace(/\{uuidName\}/g, item.id));
                            }
                        });
                        toastr.success('上传成功!');
                    } else {
                        toastr.error(data.data, '上传失败!');
                    }

                });
                this.on("error", function(file, errorMessage, xhr) {
                    toastr.error(errorMessage, '上传失败!');
                });
                this.on("complete", function(file) {
                    this.removeFile(file);
                });
            }
        });
    }

    initSimpleMDE(textareaDom) {
        this.simplemde = new SimpleMDE({
            element: textareaDom,
            spellChecker: false,
            status: false,
            autofocus: true,
            toolbar: false,
            forceSync: true,
            autoDownloadFontAwesome: false,
            insertTexts: {
                table: ["", "\n\n| 列1 | 列2 | 列3 |\n| ------ | ------ | ------ |\n| 文本 | 文本 | 文本 |\n\n"],
            },
            previewRender: (plainText, preview) => { // Async method
                return this.simplemde.markdown(utils.preParse(plainText));
            },
        });

        this.$chatMsgInputRef = $(this.inputRef).find('.textareaWrapper .CodeMirror textarea');
        this.initTextcomplete();
    }

    initTextcomplete() {

        $(this.$chatMsgInputRef).textcomplete([{ // chat msg help
            match: /(|\b)(\/.*)$/,
            search: (term, callback) => {
                var keys = _.keys(tips);
                callback($.map(keys, (key) => {
                    return key.indexOf(term) === 0 ? key : null;
                }));
            },
            template: (value, term) => {
                return tips[value].label;
            },
            replace: (value) => {
                if (this.tipsActionHandler(value)) {
                    return `$1${tips[value].value}`;
                } else {
                    return '';
                }
            }
        }, { // @user
            match: /(^|\s)@(\w*)$/,
            search: (term, callback) => {
                callback($.map(this.members, (member) => {
                    return member.username.indexOf(term) === 0 ? member.username : null;
                }));
            },
            template: (value, term) => {
                let user = _.find(this.members, { username: value });
                return `${user.name} - ${user.mails} (${user.username})`;
            },
            replace: (value) => {
                return `$1{~${value}}`;
            }
        }], {
            appendTo: '.tms-chat-status-bar',
            maxCount: 25
        });

        this.simplemde.codemirror.on('keydown', (cm, e) => {
            if (_.includes([13, 38, 40], e.keyCode) && this.isTipsShow()) { // enter | up | down
                e.preventDefault();
            } else if (e.ctrlKey && e.keyCode === 13) {
                this.sendChatMsg();
            } else if (e.keyCode === 27) {
                this.simplemde.value('');
            } else if (e.ctrlKey && e.keyCode == 85) {
                $(this.btnItemUploadRef).find('.content').click();
            } else if (e.ctrlKey && e.keyCode == 191) {
                this.emHotkeysModal.show();
            }
        });
    }

    sendChatMsg() {

        let content = this.simplemde.value();

        if (!$.trim(content)) {
            this.simplemde.value('');
            return;
        }

        if (this.sending) {
            return;
        }

        this.sending = true;

        var html = utils.md2html(content);

        let url;
        let data;
        if (this.isAt) {
            url = `/admin/chat/direct/create`;
            data = {
                baseUrl: utils.getBaseUrl(),
                path: wurl('path'),
                chatTo: this.chatTo,
                content: content,
                contentHtml: html
            };
        } else {
            url = `/admin/chat/channel/create`;
            data = {
                url: utils.getUrl(),
                channelId: this.channel.id,
                usernames: utils.parseUsernames(content, this.members).join(','),
                content: content,
                contentHtml: html
            };
        }
        $.post(url, data, (data, textStatus, xhr) => {
            if (data.success) {
                this.simplemde.value('');
                ea.publish(nsCons.EVENT_CHAT_MSG_SENDED, {
                    data: data
                });
            } else {
                toastr.error(data.data, '发送消息失败!');
            }
        }).always(() => {
            this.sending = false;
        });
    }

    sendChatMsgHandler() {
        this.sendChatMsg();
    }

    isTipsShow() {
        return $(this.chatStatusBarRef).find('.textcomplete-dropdown:visible').size() === 1;
    }

    /**
     * 编辑器插入自定义沟通内容
     * @param  {[type]} cm      [description]
     * @param  {[type]} tip [description]
     * @return {[type]}         [description]
     */
    insertTipContent(tip, mde) {
        let cm = mde ? mde.codemirror : this.simplemde.codemirror;
        var cursor = cm.getCursor();
        var line = cm.getLine(cursor.line);
        var indexSlash = _.lastIndexOf(line, '/', cursor.ch);
        if (cursor) {
            cm.replaceRange(tip.value, {
                ch: indexSlash,
                line: cursor.line
            }, cursor);
            cm.focus();

            // TODO bug:奇怪被填充前面的字符
            if (tip.ch || tip.line) {
                cm.setCursor({
                    line: tip.line ? (cursor.line + tip.line) : cm.getCursor().line,
                    ch: tip.ch ? (indexSlash + tip.ch) : 0
                });
            }

        }
    }

    /**
     * 编辑器插入自定义沟通内容
     * @param  {[type]} cm      [description]
     * @param  {[type]} comment [description]
     * @return {[type]}         [description]
     */
    insertContent(content, mde) {
        let cm = mde ? mde.codemirror : this.simplemde.codemirror;
        var cursor = cm.getCursor();
        if (cursor) {
            cm.replaceRange(content, cursor, cursor);
            cm.focus();
        }
    }

    tipsActionHandler(value) {
        if (value == '/upload') {
            $(this.btnItemUploadRef).find('.content').click();
        } else if (value == '/shortcuts') {
            this.emHotkeysModal.show();
        } else {
            // this.insertTipContent(tips[value]);
            return true;
        }

        return false;
    }

}
