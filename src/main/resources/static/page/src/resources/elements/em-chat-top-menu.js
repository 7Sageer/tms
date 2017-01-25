import { bindable, containerless } from 'aurelia-framework';

@containerless
export class EmChatTopMenu {

    @bindable loginUser;
    @bindable users;
    @bindable channels;
    @bindable channel;
    @bindable loginUser;
    @bindable chatId;
    @bindable chatTo;
    @bindable isAt;
    isRightSidebarShow = false;
    activeType = ''; // 触发激活的操作类型: search | stow | at

    ACTION_TYPE_SEARCH = nsCons.ACTION_TYPE_SEARCH;
    ACTION_TYPE_STOW = nsCons.ACTION_TYPE_STOW;
    ACTION_TYPE_AT = nsCons.ACTION_TYPE_AT;
    ACTION_TYPE_DIR = nsCons.ACTION_TYPE_DIR;

    newAtCnt = 0;

    chatToChanged() {
        $(this.chatToDropdownRef).dropdown('set selected', this.chatTo);
    }

    /**
     * 构造函数
     */
    constructor() {
        this.subscribe = ea.subscribe(nsCons.EVENT_CHAT_MSG_WIKI_DIR, (payload) => {
            this.dir = payload.dir;

            if ((this.activeType == this.ACTION_TYPE_DIR) && this.isRightSidebarShow) {
                ea.publish(nsCons.EVENT_CHAT_SHOW_DIR, {
                    action: this.activeType,
                    result: this.dir
                });
            }
        });

        this.subscribe1 = ea.subscribe(nsCons.EVENT_CHAT_AT_NEW_CNT_UPDATE, (payload) => {
            this.newAtCnt = payload.newAtCnt;
        });
    }

    /**
     * 当数据绑定引擎从视图解除绑定时被调用
     */
    unbind() {
        this.subscribe.dispose();
        this.subscribe1.dispose();
    }

    /**
     * 当视图被附加到DOM中时被调用
     */
    attached() {
        this.initHotkeys();
        this.initSearch();
    }

    initSearch() {
        var source = [];
        if (localStorage) {
            var v = localStorage.getItem('tms/chat-direct:search');
            source = v ? $.parseJSON(v) : [];
        }
        this.searchSource = source;
        $(this.searchRef).search({
            source: source,
            onSelect: (result, response) => {
                this.searchHandler();
            },
            onResults: () => {
                $(this.searchRef).search('hide results');
            }
        });

    }

    searchHandler() {

        $(this.searchRef).search('hide results');

        let search = $(this.searchInputRef).val();

        if (!search || search.length < 2) {
            toastr.error('检索条件至少需要两个字符!');
            return;
        }

        this.search = search;

        // 保存检索值
        var isExists = false;
        $.each(this.searchSource, function(index, val) {
            if (val.title == search) {
                isExists = true;
                return false;
            }
        });
        if (!isExists) {
            this.searchSource.splice(0, 0, {
                title: search
            });
            $(this.searchRef).search({
                source: _.clone(this.searchSource)
            });
        }
        localStorage && localStorage.setItem('tms/chat-direct:search', JSON.stringify(this.searchSource));

        let url;
        let data;
        if (this.isAt) {
            url = `/admin/chat/direct/search`;
            data = {
                search: this.search,
                size: 20,
                page: 0
            };
        } else {
            url = `/admin/chat/channel/search`;
            data = {
                search: this.search,
                channelId: this.channel.id,
                size: 20,
                page: 0
            };
        }

        this.searchingP = $.get(url, data, (data) => {
            if (data.success) {
                this.toggleRightSidebar(true);

                ea.publish(nsCons.EVENT_CHAT_SEARCH_RESULT, {
                    action: this.activeType,
                    result: data.data,
                    search: this.search
                });
            }
        });
    }

    initHotkeys() {
        $(document).bind('keydown', 'ctrl+.', (event) => {
            event.preventDefault();
            this.toggleRightSidebar();
        }).bind('keydown', 'ctrl+k', (event) => {
            event.preventDefault();
            $(this.chatToDropdownRef).dropdown('toggle');
        });

        $(this.filterChatToUser).bind('keydown', 'ctrl+k', (event) => {
            event.preventDefault();
            $(this.chatToDropdownRef).dropdown('toggle');
        });
    }

    initChatToDropdownHandler(last) {
        if (last) {
            _.defer(() => {
                $(this.chatToDropdownRef).dropdown().dropdown('set selected', this.chatTo).dropdown({
                    onChange: (value, text, $choice) => {
                        window.location = wurl('path') + `#/chat/${$choice.attr('data-id')}`;
                    }
                });
            });
        }
    }

    searchFocusHandler() {
        $(this.searchInputRef).css('width', 'auto');
        $(this.searchRemoveRef).show();
        this.isActiveSearch = true;
    }

    searchBlurHandler() {
        if (!$(this.searchInputRef).val()) {
            $(this.searchInputRef).css('width', '100px');
            $(this.searchRemoveRef).hide();
            this.isActiveSearch = false;
        }
    }

    sibebarRightHandler() {
        this.toggleRightSidebar();
    }

    toggleRightSidebar(asShow) {
        if (_.isUndefined(asShow)) {
            this.isRightSidebarShow = !this.isRightSidebarShow;
        } else {
            this.isRightSidebarShow = asShow;
        }

        ea.publish(nsCons.EVENT_CHAT_SIDEBAR_TOGGLE, {
            isShow: this.isRightSidebarShow
        });
    }

    searchKeyupHandler(evt) {
        if (evt.keyCode === 13) {
            this.activeType = nsCons.ACTION_TYPE_SEARCH;
            this.searchHandler();
        }
        return true;
    }

    clearSearchHandler() {
        $(this.searchInputRef).val('').focus();
    }

    showStowHandler() {
        this.activeType = nsCons.ACTION_TYPE_STOW;
        $.get('/admin/chat/channel/getStows', (data) => {
            if (data.success) {
                let stowChats = _.map(data.data, (item) => {
                    let chatChannel = item.chatChannel;
                    chatChannel.chatStow = item;
                    return chatChannel;
                });
                ea.publish(nsCons.EVENT_CHAT_SHOW_STOW, {
                    action: this.activeType,
                    result: _.reverse(stowChats)
                });
                this.toggleRightSidebar(true);
            } else {
                toastr.error(data.data, '获取收藏消息失败!');
            }
        });
    }

    showAtHandler() {
        this.activeType = nsCons.ACTION_TYPE_AT;
        this.newAtCnt = 0;
        $.get('/admin/chat/channel/getAts', {
            page: 0,
            size: 20
        }, (data) => {
            if (data.success) {
                ea.publish(nsCons.EVENT_CHAT_SHOW_AT, {
                    action: this.activeType,
                    result: data.data
                });
                this.toggleRightSidebar(true);
            } else {
                toastr.error(data.data, '获取@消息失败!');
            }
        });
    }

    logoutHandler() {
        $.post('/admin/logout').always(() => {
            utils.redirect2Login();
        });
    }

    showWikiDirHandler() {
        this.activeType = nsCons.ACTION_TYPE_DIR;
        ea.publish(nsCons.EVENT_CHAT_SHOW_DIR, {
            action: this.activeType,
            result: this.dir
        });
        this.toggleRightSidebar(true);
    }

    userEditHandler() {
        this.userEditMd.show();
    }
}
