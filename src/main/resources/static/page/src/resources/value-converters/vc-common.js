import 'jquery-format';
import 'timeago';

let tg = timeago();

/**
 * 该文件用于定义值的过滤转换器
 *
 */
// ============================================================
/**
 * 转换为大写形式
 * eg: <p>${name | upper}</p>
 */
export class UpperValueConverter {
    toView(value) {
        return value && value.toUpperCase();
    }
}

/**
 * 转换为小写形式
 * eg: <p>${name | lower}</p>
 */
export class LowerValueConverter {
    toView(value) {
        return value && value.toLowerCase();
    }
}

/**
 * 时间格式化值转换器, using as: 4234234234 | dateFormat
 * doc: https://www.npmjs.com/package/jquery-format
 */
export class DateValueConverter {
    toView(value, format = 'yyyy-MM-dd hh:mm:ss') {
        return _.isInteger(_.toNumber(value)) ? $.format.date(new Date(value), format) : (value ? value : '');
    }
}

/**
 * 数值格式化值转换器, using as: 4234234234 | numberFormat
 * doc: https://www.npmjs.com/package/jquery-format
 */
export class NumberValueConverter {
    toView(value, format = '#,##0.00') {
        return _.isNumber(_.toNumber(value)) ? $.format.number(value, format) : (value ? value : '');
    }
}

/**
 * 日期timeago值转换器
 * doc: 
 * https://www.npmjs.com/package/better-timeago
 * https://www.npmjs.com/package/better-timeago-locale-zh-cn
 */
export class TimeagoValueConverter {
    toView(value) {
        return value ? tg.format(value, 'zh_CN') : '';
    }
}

/**
 * markdown内容解析处理
 */
export class ParseMdValueConverter {
    toView(value, editor = null) {
        if (editor == 'Html') {
            return value ? value : '';
        }
        return value ? marked(utils.preParse(value)) : '';
    }
}

export class SortValueConverter {
    toView(value, prop) {
        return _.isArray(value) ? _.sortBy(value, prop) : value;
    }
}

export class SortBlogValueConverter {
    toView(value, prop = 'title') {

        if (!_.isArray(value) || value.length == 0) return value;

        if (_.some(value, item => !_.isNil(item.sort))) { // 数组中任意一个元素包含sort值，表示排过序
            return _.sortBy(value, ['sort', prop]);
        }

        return _.sortBy(value, prop);
    }
}

export class SortUsersValueConverter {
    toView(value, username) {
        if (_.isArray(value) && username) {
            let user = _.find(value, { username: username });
            if (user) {
                return [user, ..._.reject(value, { username: username })];
            }
        }
        return value;
    }
}

export class SortUsernamesValueConverter {
    toView(value, username) {
        if (_.isArray(value) && username) {
            if (_.includes(value, username)) {
                return [username, ..._.without(value, username)];
            }
        }
        return value;
    }
}

export class SortChannelsValueConverter {
    toView(value) {
        if (_.isArray(value)) {
            let channelAll = _.find(value, { name: 'all' });
            if (channelAll) {
                return [channelAll, ..._.reject(value, { name: 'all' })]
            }
        }
        return value;
    }
}

export class UserNameValueConverter {
    toView(value) {
        let user = _.find(window.tmsUsers, { username: value });
        if (user) {
            return user.name;
        }
        return value;
    }
}

export class EmojiValueConverter {
    toView(value, mkbodyDom) {
        if (emojify) {
            _.defer(() => {
                emojify.run(mkbodyDom);
            });
        }
        return value;
    }
}

export class Nl2brValueConverter {
    toView(value) {
        if (value) {
            return _.replace(value, /\n/g, '<br/>');
        }
        return value;
    }
}

export class DiffHtmlValueConverter {
    toView(value, allowedTags, allowedAttributes) {
        if (value) {
            return utils.diffHtml(value);
        }
        return value;
    }
}

function parseImgSrc(content) {
    //1，匹配出图片img标签（即匹配出所有图片），过滤其他不需要的字符
    //2.从匹配出来的结果（img标签中）循环匹配出图片地址（即src属性）
    //匹配图片（g表示匹配所有结果i表示区分大小写）
    var imgReg = /<img.*?(?:>|\/>)/gi;
    //匹配src属性
    var srcReg = /src=[\'\"]?([^\'\"]*)[\'\"]?/i;
    var arr = content.match(imgReg);
    if (arr) {
        for (var i = 0; i < arr.length; i++) {
            var src = arr[i].match(srcReg);
            if (src && src.length > 1) {
                return src[1];
            }
        }
    }

    return null;
}

export class ParseImgValueConverter {
    toView(value, editor, defaultImg = 'img/img.png') {
        if (editor == 'Html') {
            let src = parseImgSrc(value);
            // console.log(src);
            return src;
        }

        let r = /\!\[.*\]\((.*)\)/;
        let v = r.exec(value);
        // console.log(v[1]);
        return (v && (v.length > 1)) ? v[1] : defaultImg;
    }
}

export class ExistImgValueConverter {
    toView(value, editor) {
        if (editor == 'Html') {
            return parseImgSrc(value) != null;
        }

        let r = /\!\[.*\]\((.*)\)/;
        let v = r.exec(value);
        // console.log(v)
        return (v && (v.length > 1)) ? true : false;
    }
}
