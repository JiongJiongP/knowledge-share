# React 18 并发特性深入解析

> **作者：** 李明 &nbsp;|&nbsp; **日期：** 2026-05-12 &nbsp;|&nbsp; **标签：** `技术` `前端`

---

## 1. 并发渲染概述

React 18 引入了全新的**并发渲染机制**，这是一个底层架构的重大升级。并发模式允许 React 同时准备多个版本的 UI，并根据优先级进行渲染。

在传统的同步渲染中，一旦开始渲染，就必须完成整个渲染过程才能响应用户输入。而并发渲染允许 React 在渲染过程中**暂停、中止或复用**正在进行的工作。

### 核心概念

| 概念 | 说明 |
|------|------|
| **可中断渲染** | React 可以在渲染过程中暂停，处理更高优先级的更新 |
| **优先级调度** | 用户交互（点击、输入）优先级高于数据加载 |
| **自动批处理** | 所有状态更新都会自动批处理，包括 Promise 和 setTimeout 中的更新 |
| **流式 SSR** | 服务端渲染支持流式传输和选择性注水 |

---

## 2. Suspense 改进

在 React 18 中，Suspense 不再需要配合 SSR 使用，可以在**纯客户端场景**中独立工作。通过 `fallback` 属性，可以优雅地处理异步组件的加载状态。

```jsx
import { Suspense } from 'react';

function App() {
  return (
    <Suspense fallback={<LoadingSpinner />}>
      <AsyncDashboard />
    </Suspense>
  );
}
```

### 数据获取最佳实践

```jsx
// 使用 Suspense 配合 React Query 或 Relay
function ProfilePage() {
  return (
    <Suspense fallback={<ProfileSkeleton />}>
      <ProfileDetails />
      <Suspense fallback={<PostSkeleton />}>
        <ProfilePosts />
      </Suspense>
    </Suspense>
  );
}
```

> **注意：** Suspense 需要组件在数据就绪前抛出 Promise，推荐使用 React Query、SWR 或 Relay 等支持 Suspense 的数据获取库。

---

## 3. useTransition API

`useTransition` 是 React 18 新增的核心 Hook，它允许将某些状态更新标记为**非紧急更新（transition）**，从而避免阻塞用户交互。

```jsx
const [isPending, startTransition] = useTransition();

function handleSearch(input) {
  // 紧急更新：立即更新输入框
  setInputValue(input);

  // 非紧急更新：可以被中断
  startTransition(() => {
    setSearchQuery(input);
  });
}
```

### 适用场景

- 🔍 **搜索筛选** — 输入框保持响应，搜索结果延迟更新
- 📑 **标签页切换** — 点击反馈即时，内容加载可中断
- 🎨 **主题切换** — 避免复杂 UI 重渲染阻塞用户操作

### 性能对比

```jsx
// ❌ 没有 useTransition — 输入可能卡顿
function SlowSearch() {
  const [query, setQuery] = useState('');

  return (
    <>
      <input onChange={e => setQuery(e.target.value)} />
      <ExpensiveList query={query} />  {/* 渲染阻塞输入 */}
    </>
  );
}

// ✅ 使用 useTransition — 输入始终流畅
function FastSearch() {
  const [query, setQuery] = useState('');
  const [isPending, startTransition] = useTransition();

  return (
    <>
      <input onChange={e =>
        startTransition(() => setQuery(e.target.value))
      } />
      {isPending && <Spinner />}
      <ExpensiveList query={query} />
    </>
  );
}
```

---

## 4. useDeferredValue

与 `useTransition` 类似，`useDeferredValue` 允许**延迟更新某个值的渲染**，适用于需要保持响应性的搜索/筛选场景。

```jsx
import { useDeferredValue } from 'react';

function SearchResults({ query }) {
  const deferredQuery = useDeferredValue(query);
  const isStale = query !== deferredQuery;

  return (
    <div style={{ opacity: isStale ? 0.5 : 1 }}>
      <ExpensiveResultList query={deferredQuery} />
    </div>
  );
}
```

### useTransition vs useDeferredValue

| | useTransition | useDeferredValue |
|---|---|---|
| **控制方式** | 包装 state 更新函数 | 包装 state 值本身 |
| **使用场景** | 你能控制 setState 的调用 | Props 来自外部，你无法控制更新 |
| **pending 状态** | `isPending` 标志 | 新旧值不相等即为 stale |

---

## 5. 迁移指南

### 从 React 17 升级

1. **升级依赖**

```bash
npm install react@18 react-dom@18
```

2. **启用并发特性**

```jsx
// index.js — 替换 ReactDOM.render
import { createRoot } from 'react-dom/client';

const root = createRoot(document.getElementById('root'));
root.render(<App />);
```

3. **逐步采用并发特性**

| 阶段 | 动作 |
|------|------|
| 第一阶段 | 启用 `createRoot`，享受自动批处理 |
| 第二阶段 | 在搜索/筛选场景使用 `useTransition` |
| 第三阶段 | 用 `Suspense` 包裹懒加载和数据获取组件 |
| 第四阶段 | 评估 `useDeferredValue` 优化场景 |

### 常见问题

**Q: 所有应用都需要并发模式吗？**

不需要。对于简单的应用，React 18 的自动批处理就已经是不错的提升。并发特性更适合有复杂 UI 交互和数据加载的应用。

**Q: Suspense 和 Error Boundary 如何配合？**

```jsx
<ErrorBoundary fallback={<ErrorPage />}>
  <Suspense fallback={<Loading />}>
    <AsyncComponent />
  </Suspense>
</ErrorBoundary>
```

---

## 总结

React 18 的并发特性为前端开发带来了全新的性能优化手段。核心要点：

- `useTransition` 用于**标记非紧急更新**，保持交互流畅
- `useDeferredValue` 用于**延迟 Props 派生**的渲染
- `Suspense` 不再依赖 SSR，可以独立处理异步加载
- 所有更新**自动批处理**，无需手动优化

> 建议从 `createRoot` 开始逐步迁移，不必一次性引入所有并发特性。
