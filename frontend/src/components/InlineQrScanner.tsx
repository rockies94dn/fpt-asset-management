import { useEffect, useRef, useState } from 'react'
import QrScanner from 'qr-scanner'

type InlineQrScannerProps = {
  autoStart?: boolean
  onDetected: (code: string) => void
  compact?: boolean
  active?: boolean
  onActiveChange?: (active: boolean) => void
  showControls?: boolean
}

export function InlineQrScanner({
  autoStart = false,
  onDetected,
  compact = false,
  active,
  onActiveChange,
  showControls = true,
}: InlineQrScannerProps) {
  const [internalCameraEnabled, setInternalCameraEnabled] = useState(autoStart)
  const [scannerActive, setScannerActive] = useState(false)
  const [scanError, setScanError] = useState<string | null>(null)
  const [lastScannedCode, setLastScannedCode] = useState<string | null>(null)
  const [hasCameraSupport, setHasCameraSupport] = useState<boolean | null>(null)
  const videoRef = useRef<HTMLVideoElement | null>(null)
  const scannerRef = useRef<QrScanner | null>(null)
  const onDetectedRef = useRef(onDetected)

  const cameraEnabled = active ?? internalCameraEnabled
  const setCameraEnabled = onActiveChange ?? setInternalCameraEnabled

  useEffect(() => {
    onDetectedRef.current = onDetected
  }, [onDetected])

  useEffect(() => {
    let cancelled = false

    void QrScanner.hasCamera()
      .then((available) => {
        if (!cancelled) {
          setHasCameraSupport(available)
        }
      })
      .catch(() => {
        if (!cancelled) {
          setHasCameraSupport(false)
        }
      })

    return () => {
      cancelled = true
    }
  }, [])

  useEffect(() => {
    if (!autoStart) return
    setCameraEnabled(true)
  }, [autoStart, setCameraEnabled])

  useEffect(() => {
    if (!videoRef.current) return

    const scanner = new QrScanner(
      videoRef.current,
      (result) => {
        const rawValue = result.data?.trim()
        if (!rawValue) return

        const match = rawValue.match(/\/assets\/scan\/(.+)$/)
        const code = (match?.[1] ?? rawValue).trim()
        setLastScannedCode(code)
        setScanError(null)
        setScannerActive(false)
        scanner.stop()
        setCameraEnabled(false)
        onDetectedRef.current(code)
      },
      {
        preferredCamera: 'environment',
        maxScansPerSecond: 12,
        returnDetailedScanResult: true,
        onDecodeError: (error) => {
          const message = typeof error === 'string' ? error : error.message
          if (message === QrScanner.NO_QR_CODE_FOUND) return
          setScanError('Không thể đọc QR từ camera trên thiết bị này.')
        },
      },
    )

    scannerRef.current = scanner

    return () => {
      if (scannerRef.current === scanner) {
        scannerRef.current = null
      }
      scanner.destroy()
    }
  }, [setCameraEnabled])

  useEffect(() => {
    if (!scannerRef.current) return
    if (hasCameraSupport !== true) {
      if (hasCameraSupport === false) {
        setScannerActive(false)
      }
      return
    }

    let cancelled = false

    async function syncScannerState() {
      if (!scannerRef.current) return

      if (cameraEnabled) {
        try {
          setScanError(null)
          await scannerRef.current.start()
          if (!cancelled) {
            setScannerActive(true)
          }
        } catch {
          if (!cancelled) {
            setScannerActive(false)
            setScanError('Không thể mở camera. Kiểm tra quyền truy cập hoặc nhập mã QA thủ công.')
            setCameraEnabled(false)
          }
        }
      } else {
        scannerRef.current.stop()
        if (!cancelled) {
          setScannerActive(false)
        }
      }
    }

    void syncScannerState()

    return () => {
      cancelled = true
    }
  }, [cameraEnabled, hasCameraSupport, setCameraEnabled])

  return (
    <div className="d-grid gap-2">
      <div className="d-flex gap-2">
        {showControls ? (
          !cameraEnabled ? (
            <button type="button" className="btn btn-sm btn-outline-primary" onClick={() => setCameraEnabled(true)} disabled={hasCameraSupport === false}>
              <i className="bi bi-camera me-1"></i>
              {compact ? 'Quét QR' : 'Mở camera'}
            </button>
          ) : (
            <button type="button" className="btn btn-sm btn-outline-secondary" onClick={() => setCameraEnabled(false)}>
              <i className="bi bi-stop-circle me-1"></i>
              Dừng camera
            </button>
          )
        ) : null}
      </div>

      {hasCameraSupport === false ? (
        <div className="form-text">Thiết bị hoặc trình duyệt này không hỗ trợ mở camera trực tiếp. Hãy nhập mã QA thủ công.</div>
      ) : null}

      {cameraEnabled ? (
        <div className={`qr-scanner-preview${compact ? ' qr-scanner-preview-sm' : ''}`}>
          <video ref={videoRef} className="w-100 h-100 object-fit-cover" muted playsInline autoPlay />
        </div>
      ) : null}

      {scannerActive && !scanError ? <div className="form-text">Đưa mã QR vào giữa khung hình để quét.</div> : null}

      {lastScannedCode ? (
        <div className="alert alert-success py-2 mb-0">
          <i className="bi bi-check-circle me-2"></i>
          Đã quét: <strong>{lastScannedCode}</strong>
        </div>
      ) : null}

      {scanError ? <div className="toast">{scanError}</div> : null}
    </div>
  )
}
