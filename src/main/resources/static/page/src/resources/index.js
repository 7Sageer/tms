/* 
代码生成常用命令:
au generate element
au generate attribute
au generate value-converter
au generate binding-behavior
au generate task
au generate generator
*/
/* 加载全局资源 */
export function configure(aurelia) {

    aurelia.globalResources([
        'resources/value-converters/vc-common',
        'resources/binding-behaviors/bb-key',
        'resources/attributes/attr-task',
        'resources/attributes/attr-fancybox',
        'resources/attributes/attr-pastable',
        'resources/attributes/attr-autosize',
        'resources/attributes/attr-dropzone',
        'resources/attributes/attr-attr',
        'resources/attributes/attr-c2c',
        'resources/attributes/attr-dimmer',
        'resources/attributes/attr-ui-dropdown',
        'resources/attributes/attr-ui-dropdown-action',
        'resources/attributes/attr-ui-dropdown-hover',
        'resources/attributes/attr-ui-tab',
        'resources/attributes/attr-tablesort',
        'resources/attributes/attr-textcomplete',
        'resources/attributes/attr-scrollbar',
        'resources/elements/em-modal',
        'resources/elements/em-dropdown',
        'resources/elements/em-confirm-modal',
        'resources/elements/em-user-avatar',
        'resources/elements/em-blog-list',
        'resources/elements/em-header',
        'resources/elements/em-footer',
        'resources/elements/em-blog-summary',
        'resources/elements/em-blog-content',
        'resources/elements/em-blog-comment',
        'resources/elements/em-blog-dir',
        'resources/elements/em-blog-mind',
        'resources/elements/em-blog-excel',
        'resources/elements/em-blog-sheet',
        'resources/elements/em-blog-share-ppt-content',
    ]);
}
