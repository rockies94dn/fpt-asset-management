import { useQuery } from '@tanstack/react-query'
import { api, setCsrfToken } from '../api/client'

export function useSession() {
  return useQuery({
    queryKey: ['session'],
    queryFn: async () => {
      const response = await api.me()
      setCsrfToken(response.csrfToken)
      return response
    },
    staleTime: 60_000,
    retry: false,
  })
}
