import { clsx, type ClassValue } from 'clsx'
import { twMerge } from 'tailwind-merge'

export function cn(...inputs: ClassValue[]) {
  return twMerge(clsx(inputs))
}

export function formatTsh(amount: number | null | undefined) {
  if (amount == null) return '—'
  return `TSh ${Number(amount).toLocaleString()}`
}
