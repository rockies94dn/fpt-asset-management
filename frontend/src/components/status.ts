export function statusClassName(status: string | null | undefined): string {
  return status?.toLowerCase() ?? 'unknown'
}
