# CareerHub MySQL 索引与事务设计初版

## 1. User 表索引

### 主键索引
- `id` 为 PRIMARY KEY。
- 用于唯一标识用户记录。

### username
- `username` 设置 UNIQUE。
- 数据库已经自动建立唯一索引。
- 注册、登录、用户名重复检查都会高频按 username 精确查询。
- 不需要再额外创建普通索引。

### email
- `email` 设置 UNIQUE。
- 数据库已经自动建立唯一索引。
- 用于邮箱登录或邮箱重复检查。
- 不需要再额外创建普通索引。

### status
- 当前暂不单独建立索引。
- ACTIVE / DISABLED 可选值少，区分度较低。
- 后续结合真实查询场景再决定。

## 2. 联合索引

联合索引需要根据实际查询条件设计。

例如 `(username, status)`：
- 查询 username 可以利用索引。
- 查询 username + status 可以利用索引。
- 单独查询 status 不能很好利用该联合索引。

当前 CareerHub 暂无必要额外增加联合索引。

## 3. 注册事务边界

注册如果只插入一条 user 数据，不需要人为拆成复杂事务。

如果后续注册包含：
1. 创建 User
2. 创建 UserProfile
3. 创建默认配置

这些操作必须一起成功，因此应放在同一事务中：
- 全部成功：COMMIT
- 任一失败：ROLLBACK

发送欢迎邮件等外部服务调用不直接放入数据库事务，
应在数据库事务成功提交之后处理。

username/email 的唯一性最终由数据库 UNIQUE 约束兜底。