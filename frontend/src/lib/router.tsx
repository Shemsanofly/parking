import {
  type AnchorHTMLAttributes,
  type MouseEvent,
  type ReactNode,
  useEffect,
  useMemo,
  useState,
} from 'react'
import { currentPath, normalizePath, RouterContext, type RouterContextValue } from './router-context'
import { useNavigate, usePathname } from './router-hooks'

export function BrowserRouter({ children }: { children: ReactNode }) {
  const [path, setPath] = useState(currentPath)

  useEffect(() => {
    const handlePopState = () => setPath(currentPath())
    window.addEventListener('popstate', handlePopState)
    return () => window.removeEventListener('popstate', handlePopState)
  }, [])

  const value = useMemo<RouterContextValue>(
    () => ({
      path,
      navigate(to, options) {
        const nextUrl = new URL(to, window.location.origin)
        const next = `${nextUrl.pathname}${nextUrl.search}${nextUrl.hash}`
        if (next === currentPath()) {
          return
        }
        if (options?.replace) {
          window.history.replaceState(null, '', next)
        } else {
          window.history.pushState(null, '', next)
        }
        setPath(next)
      },
    }),
    [path],
  )

  return <RouterContext.Provider value={value}>{children}</RouterContext.Provider>
}

export function Navigate({ to, replace = false }: { to: string; replace?: boolean }) {
  const navigate = useNavigate()

  useEffect(() => {
    navigate(to, { replace })
  }, [navigate, replace, to])

  return null
}

type LinkProps = Omit<AnchorHTMLAttributes<HTMLAnchorElement>, 'href'> & {
  to: string
}

export function Link({ to, onClick, target, ...props }: LinkProps) {
  const navigate = useNavigate()

  function handleClick(event: MouseEvent<HTMLAnchorElement>) {
    onClick?.(event)
    if (
      event.defaultPrevented ||
      event.button !== 0 ||
      target ||
      event.metaKey ||
      event.altKey ||
      event.ctrlKey ||
      event.shiftKey
    ) {
      return
    }
    event.preventDefault()
    navigate(to)
  }

  return <a href={to} onClick={handleClick} target={target} {...props} />
}

type NavLinkProps = Omit<LinkProps, 'className'> & {
  className?: string | ((state: { isActive: boolean }) => string)
  end?: boolean
}

export function NavLink({ className, end = false, to, ...props }: NavLinkProps) {
  const pathname = usePathname()
  const target = normalizePath(to)
  const isActive = end ? pathname === target : pathname === target || pathname.startsWith(`${target}/`)
  const resolvedClassName = typeof className === 'function' ? className({ isActive }) : className

  return <Link className={resolvedClassName} to={to} {...props} />
}
