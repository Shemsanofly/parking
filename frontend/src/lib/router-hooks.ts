import { useContext } from 'react'
import { normalizePath, RouterContext } from './router-context'

function useRouter() {
  const context = useContext(RouterContext)
  if (!context) {
    throw new Error('Router components must be used inside BrowserRouter')
  }
  return context
}

export function usePathname() {
  return normalizePath(useRouter().path)
}

export function useNavigate() {
  return useRouter().navigate
}
