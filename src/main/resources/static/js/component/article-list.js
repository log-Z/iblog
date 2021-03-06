import Pagination from '/static/js/component/pagination.js'

export default {
    components: {
        pagination: Pagination
    },
    props: {
        baseUrl: {
            type: String,
            default: function () {
                return '/api/article/list';
            },
        },
        fields: {
            type: Array,
            default: function () {
                return ['title', 'createTime'];
            }
        },
        supportOptions: {
            type: Array,
            default: function () {
                return ['view'];
            },
        },
        requestParam: {
            type: Object,
            default: function() {
                return {
                    articleId: null,
                    authorId: null,
                    title: null,
                    content: null,
                    createTime: null,
                    fuzzySearch: null,
                }
            },
        },
        pageRange: {
            type: Object,
            default: function() {
                return {
                    pageNum: null,
                    pageSize: null,
                }
            },
        },
        refresh: {
            type: Number,
            default: null,
        },
    },
    data: function() {
        return {
            message: null,
            articles: [],
            resPageRange: {
                pageNum: null,
                pages: null,
                pageSize: null,
                total: null,
            },
        }
    },
    computed: {
        fieldIdEnable: function () {
            return this.fields.includes('id');
        },
        fieldTitleEnable: function () {
            return this.fields.includes('title');
        },
        fieldCreateTimeEnable: function () {
            return this.fields.includes('createTime');
        },
        fieldOptionsEnable: function () {
            return this.fields.includes('options');
        },
        viewable: function () {
            return this.supportOptions.includes('view');
        },
        editable: function () {
            return this.supportOptions.includes('edit');
        },
        deletable: function () {
            return this.supportOptions.includes('delete');
        },
    },
    template: `
                <div>
                    <p>{{message}}</p>
                    <table>
                        <tr>
                            <th v-if="fieldIdEnable">ID</th>
                            <th v-if="fieldTitleEnable">标题</th>
                            <th v-if="fieldCreateTimeEnable">发布日期</th>
                            <th v-if="fieldOptionsEnable">操作</th>
                        </tr>
                        <tr v-for="article in articles">
                            <td v-if="fieldIdEnable">{{article.articleId}}</td>
                            <td v-if="fieldTitleEnable">{{article.title}}</td>
                            <td v-if="fieldCreateTimeEnable">{{article.createTime}}</td>
                            <td v-if="fieldOptionsEnable">
                                <a v-if="viewable" :href="articleViewUrl(article.articleId)">[查看]</a>
                                <a v-if="editable" :href="articleEditUrl(article.articleId)">[编辑]</a>
                                <a v-if="deletable" href="#" @click.prevent="deleteArticle(article.articleId)">[删除]</a>
                            </td>
                        </tr>
                    </table>
                    <pagination :page-range="resPageRange" @updated="rangeUpdate"></pagination>
                </div>
            `,
    methods: {
        articleViewUrl: function(articleId) {
            return 'article.html#/' + articleId;
        },
        articleEditUrl: function(articleId) {
            return 'article-editor.html#/' + articleId;
        },
        articleDeleteUrl: function(articleId) {
            return '/api/article/' + articleId
        },
        reload: function () {
            this.$axios.get(this.baseUrl, {
                params: {
                    ...this.requestParam,
                    'pageRange.pageNum': this.pageRange.pageNum,
                    'pageRange.pageSize': this.pageRange.pageSize,
                },
            }).then(response => {
                if (response.status === 200) {
                    this.articles = response.data.data.articles;
                    this.resPageRange = response.data.data.pageRange;
                }
            })
        },
        deleteArticle: function (articleId) {
            const apiUrl = this.articleDeleteUrl(articleId);
            this.$axios.delete(apiUrl).then(response => {
                if (response.status === 204) {
                    this.message = '删除文章成功，ID：' + articleId;
                    this.reload();
                } else {
                    this.message = '删除文章失败，ID：' + articleId;
                }
            }).catch(error => {
                this.message = error.response.data.errors.message;
            })
        },
        rangeUpdate: function (pageNum) {
            this.$emit('range-update', pageNum, this.pageRange.pageSize)
        },
    },
    watch: {
        refresh: function() {
            this.reload();
        },
    },
}
