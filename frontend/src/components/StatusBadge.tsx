type StatusBadgeProps = {
  label: string
  tone?: 'default' | 'success' | 'danger'
}

export function StatusBadge({ label, tone = 'default' }: StatusBadgeProps) {
  return <span className={`badge${tone === 'default' ? '' : ` ${tone}`}`}>{label}</span>
}
