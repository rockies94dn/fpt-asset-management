import { useQuery } from '@tanstack/react-query'
import { useEffect, useMemo, useRef, useState } from 'react'
import { Link, useNavigate, useParams } from 'react-router-dom'
import { api } from '../api/client'
import { getAssetUsageDisabledReason } from '../components/assetUsage'
import { StatusBadge } from '../components/StatusBadge'
import { useSession } from '../hooks/useSession'

type DetectedBarcode = { rawValue?: string }

type BarcodeDetectorInstance = {
  detect: (source: ImageBitmapSource | HTMLVideoElement | HTMLCanvasElement) => Promise<DetectedBarcode[]>
}

type BarcodeDetectorConstructor = new (options?: { formats?: string[] }) => BarcodeDetectorInstance

function getBarcodeDetector() {
  return (window as Window & { BarcodeDetector?: BarcodeDetectorConstructor }).BarcodeDetector
}

export function ScanPage() {
  const navigate = useNavigate()
  const session = useSession()
  const { qaCode } = useParams()
  const [manualCode, setManualCode] = useState('')
  const [scanError, setScanError] = useState<string | null>(null)
  const [scannerReady, setScannerReady] = useState(false)
  const [scannerActive, setScannerActive] = useState(false)
  const [cameraEnabled, setCameraEnabled] = useState(false)
  const [detectedCode, setDetectedCode] = useState<string | null>(qaCode ?? null)
  const videoRef = useRef<HTMLVideoElement | null>(null)
  const streamRef = useRef<MediaStream | null>(null)
  const frameRef = useRef<number | null>(null)
  const scanningLockRef = useRef(false)

  const detectorCtor = useMemo(() => {
    if (typeof window === 'undefined') return undefined
    return getBarcodeDetector()
  }, [])

  const asset = useQuery({
    queryKey: ['scan', detectedCode],
    queryFn: () => api.scannedAsset(detectedCode!),
    enabled: Boolean(detectedCode),
  })

  useEffect(() => {
    setDetectedCode(qaCode ?? null)
  }, [qaCode])

  useEffect(() => {
    setScannerReady(Boolean(detectorCtor && navigator.mediaDevices?.getUserMedia))
  }, [detectorCtor])

  useEffect(() => {
    if (!scannerActive || !detectorCtor || !videoRef.current) return

    const detector = new detectorCtor({ formats: ['qr_code'] })

    async function scanFrame() {
      if (!videoRef.current || scanningLockRef.current) {
        frameRef.current = window.requestAnimationFrame(scanFrame)
        return
      }

      if (videoRef.current.readyState < 2) {
        frameRef.current = window.requestAnimationFrame(scanFrame)
        return
      }

      scanningLockRef.current = true
      try {
        const barcodes = await detector.detect(videoRef.current)
        const code = barcodes.find((item) => item.rawValue?.trim())?.rawValue?.trim()
        if (code) {
          setDetectedCode(code)
          setManualCode(code)
          setScannerActive(false)
          setCameraEnabled(false)
          navigate(`/scan/${encodeURIComponent(code)}`, { replace: true })
          return
        }
      } catch {
        setScanError('Không thể đọc QR từ camera trên thiết bị này.')
      } finally {
        scanningLockRef.current = false
      }

      frameRef.current = window.requestAnimationFrame(scanFrame)
    }

    frameRef.current = window.requestAnimationFrame(scanFrame)
    return () => {
      if (frameRef.current) {
        window.cancelAnimationFrame(frameRef.current)
        frameRef.current = null
      }
    }
  }, [detectorCtor, navigate, scannerActive])

  useEffect(() => {
    if (!cameraEnabled || !videoRef.current) return

    let cancelled = false

    async function startCamera() {
      try {
        setScanError(null)
        const stream = await navigator.mediaDevices.getUserMedia({
          video: { facingMode: { ideal: 'environment' } },
          audio: false,
        })
        if (cancelled) {
          stream.getTracks().forEach((track) => track.stop())
          return
        }
        streamRef.current = stream
        videoRef.current!.srcObject = stream
        await videoRef.current!.play()
        setScannerActive(true)
      } catch {
        setScanError('Không mở được camera. Kiểm tra quyền truy cập hoặc dùng nhập mã thủ công.')
        setCameraEnabled(false)
      }
    }

    void startCamera()

    return () => {
      cancelled = true
    }
  }, [cameraEnabled])

  useEffect(() => {
    if (cameraEnabled || scannerActive) return
    if (streamRef.current) {
      streamRef.current.getTracks().forEach((track) => track.stop())
      streamRef.current = null
    }
    if (videoRef.current) {
      videoRef.current.srcObject = null
    }
  }, [cameraEnabled, scannerActive])

  useEffect(() => {
    return () => {
      if (frameRef.current) {
        window.cancelAnimationFrame(frameRef.current)
      }
      if (streamRef.current) {
        streamRef.current.getTracks().forEach((track) => track.stop())
      }
    }
  }, [])

  function startScanner() {
    setScanError(null)
    setDetectedCode(null)
    setCameraEnabled(true)
  }

  function stopScanner() {
    setScannerActive(false)
    setCameraEnabled(false)
  }

  function submitManualCode() {
    const normalized = manualCode.trim()
    if (!normalized) return
    setScannerActive(false)
    setCameraEnabled(false)
    setDetectedCode(normalized)
    navigate(`/scan/${encodeURIComponent(normalized)}`)
  }

  const isAuthenticated = Boolean(session.data?.authenticated)

  if (!detectedCode) {
    return (
      <div className="login-wrapper">
        <div className="container py-5">
          <div className="scan-card">
            <div className="scan-hero">
              <div className="mb-2">Quét QR thiết bị</div>
              <h1 className="h3 mb-2">Mở camera để nhận diện mã QR</h1>
              <div>Dùng camera sau trên điện thoại để mở nhanh thông tin thiết bị.</div>
            </div>

            <div className="card">
              <div className="card-body">
                {scannerReady ? (
                  <div className="d-grid gap-3">
                    <div className="ratio ratio-4x3 rounded overflow-hidden bg-dark">
                      <video ref={videoRef} className="w-100 h-100 object-fit-cover" muted playsInline autoPlay />
                    </div>

                    <div className="d-flex gap-2">
                      {!cameraEnabled ? (
                        <button className="btn btn-primary flex-fill" onClick={startScanner}>
                          <i className="bi bi-camera me-2"></i>
                          Mở camera
                        </button>
                      ) : (
                        <button className="btn btn-outline-secondary flex-fill" onClick={stopScanner}>
                          <i className="bi bi-camera-video-off me-2"></i>
                          Tắt camera
                        </button>
                      )}
                    </div>
                  </div>
                ) : (
                  <div className="toast mb-3">
                    Trình duyệt này chưa hỗ trợ quét QR trực tiếp. Bạn vẫn có thể nhập mã QA thủ công.
                  </div>
                )}

                {scanError ? <div className="toast mt-3">{scanError}</div> : null}

                <div className="mt-4">
                  <label className="form-label">Nhập mã QA thủ công</label>
                  <div className="input-group">
                    <input
                      className="form-control"
                      placeholder="Ví dụ: FPT-001"
                      value={manualCode}
                      onChange={(event) => setManualCode(event.target.value)}
                      onKeyDown={(event) => {
                        if (event.key === 'Enter') submitManualCode()
                      }}
                    />
                    <button className="btn btn-outline-primary" onClick={submitManualCode}>
                      Mở thiết bị
                    </button>
                  </div>
                </div>

                <div className="text-muted mt-3" style={{ fontSize: '13px' }}>
                  Nếu camera đã mở mà chưa nhận QR, đưa mã vào khung hình sáng và giữ máy ổn định vài giây.
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    )
  }

  if (asset.isLoading) {
    return (
      <div className="login-wrapper">
        <div className="container py-5">
          <div className="toast">Loading scanned asset...</div>
        </div>
      </div>
    )
  }

  if (!asset.data) {
    return (
      <div className="login-wrapper">
        <div className="container py-5">
          <div className="toast">Asset not found.</div>
        </div>
      </div>
    )
  }

  const usageDisabledReason = getAssetUsageDisabledReason(asset.data)

  return (
    <div className="login-wrapper">
      <div className="container py-5">
        <div className="scan-card">
          <div className="scan-hero">
            <div className="mb-2">Thông tin thiết bị từ QR</div>
            <h1 className="h3 mb-2">{asset.data.name}</h1>
            <div>{asset.data.qaCode}</div>
          </div>

          <div className="card">
            <div className="card-body">
              <div className="d-flex align-items-center justify-content-between mb-3">
                <strong>Trạng thái</strong>
                <StatusBadge label={asset.data.statusLabel} />
              </div>
              <div className="mb-2">
                <strong>Phòng:</strong> <span className="text-muted">{asset.data.room?.name ?? 'Store room'}</span>
              </div>
              <div className="mb-3">
                <strong>Loại:</strong> <span className="text-muted">{asset.data.category?.name ?? 'N/A'}</span>
              </div>

              <div className="d-grid gap-2">
                {isAuthenticated ? (
                  <>
                    <Link className="btn btn-primary w-100" to={`/tickets?qaCode=${encodeURIComponent(asset.data.qaCode)}`}>
                      Báo hỏng thiết bị
                    </Link>
                    {usageDisabledReason ? (
                      <button className="btn btn-outline-secondary w-100" type="button" disabled title={usageDisabledReason}>
                        Không thể check-in / mượn
                      </button>
                    ) : (
                      <Link className="btn btn-outline-primary w-100" to={`/usages?qaCode=${encodeURIComponent(asset.data.qaCode)}`}>
                        Check-in / mượn thiết bị
                      </Link>
                    )}
                  </>
                ) : (
                  <Link className="btn btn-primary w-100" to="/login">
                    Đăng nhập để báo hỏng hoặc check-in
                  </Link>
                )}

                <button className="btn btn-outline-secondary w-100" onClick={() => navigate('/scan')}>
                  Quét mã khác
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  )
}
