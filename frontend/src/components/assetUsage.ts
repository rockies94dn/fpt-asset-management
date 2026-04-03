import type { Asset } from '../types/api'

type AssetUsageState = Pick<Asset, 'canBeUsed' | 'status' | 'statusLabel'>

export function getAssetUsageDisabledReason(asset: AssetUsageState | null | undefined): string | null {
  if (!asset) return null
  if (asset.canBeUsed) return null

  return asset.statusLabel
    ? `Thiết bị hiện không thể sử dụng. Trạng thái: ${asset.statusLabel}.`
    : 'Thiết bị hiện không thể sử dụng.'
}
