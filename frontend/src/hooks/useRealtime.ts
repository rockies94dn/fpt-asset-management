import { Client } from '@stomp/stompjs'
import SockJS from 'sockjs-client'
import { useEffect, useMemo, useRef } from 'react'
import { webSocketUrl } from '../config/runtime'

type SubscriptionMap = Record<string, (body: unknown) => void>

export function useRealtime(enabled: boolean, subscriptions: SubscriptionMap) {
  const subscriptionsRef = useRef(subscriptions)
  const subscriptionKeys = useMemo(
    () => Object.keys(subscriptions).sort(),
    [Object.keys(subscriptions).sort().join('|')],
  )

  useEffect(() => {
    subscriptionsRef.current = subscriptions
  }, [subscriptions])

  useEffect(() => {
    if (!enabled) {
      return
    }

    const client = new Client({
      webSocketFactory: () => new SockJS(webSocketUrl('/ws')),
      reconnectDelay: 4000,
    })

    client.onConnect = () => {
      subscriptionKeys.forEach((destination) => {
        client.subscribe(destination, (message) => {
          const callback = subscriptionsRef.current[destination]
          if (!callback) {
            return
          }
          callback(JSON.parse(message.body))
        })
      })
    }

    client.activate()
    return () => {
      client.deactivate()
    }
  }, [enabled, subscriptionKeys])
}
