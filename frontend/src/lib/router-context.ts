import { createContext } from 'react'

export type NavigateOptions = {
  replace?: boolean
}

export type RouterContextValue = {
  path: string
  navigate: (to: string, options?: NavigateOptions) => void
}

export const RouterContext = createContext<RouterContextValue | null>(null)

export function currentPath() {
  return `${window.location.pathname}${window.location.search}${window.location.hash}`
}

function pathnameOnly(path: string) {
  return path.split(/[?#]/, 1)[0] || '/'
}

export function normalizePath(path: string) {
  const pathname = pathnameOnly(path)
  return pathname.length > 1 ? pathname.replace(/\/+$/, '') : pathname
}
