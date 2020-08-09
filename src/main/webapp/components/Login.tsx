import React from 'react'
import { useLocation } from 'react-router-dom'
import queryString from 'query-string'
import { Alert, Button, Typography } from 'antd'
import styled from 'styled-components'
import Icon from '@ant-design/icons'

const { Title, Paragraph } = Typography

const DiscordSvg = () => <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="2 2 46 46">
    <path d="M40,12c0,0-4.585-3.588-10-4l-0.488,0.976C34.408,10.174,36.654,11.891,39,14c-4.045-2.065-8.039-4-15-4s-10.955,1.935-15,4c2.346-2.109,5.018-4.015,9.488-5.024L18,8c-5.681,0.537-10,4-10,4s-5.121,7.425-6,22c5.162,5.953,13,6,13,6l1.639-2.185C13.857,36.848,10.715,35.121,8,32c3.238,2.45,8.125,5,16,5s12.762-2.55,16-5c-2.715,3.121-5.857,4.848-8.639,5.815L33,40c0,0,7.838-0.047,13-6C45.121,19.425,40,12,40,12z M17.5,30c-1.933,0-3.5-1.791-3.5-4c0-2.209,1.567-4,3.5-4s3.5,1.791,3.5,4C21,28.209,19.433,30,17.5,30z M30.5,30c-1.933,0-3.5-1.791-3.5-4c0-2.209,1.567-4,3.5-4s3.5,1.791,3.5,4C34,28.209,32.433,30,30.5,30z"></path>
</svg>

const errorMessages: { [key: string]: string } = {
    'access_denied': 'Login failed because you are not a member of any SackBot enabled Discord guild.'
}

const AlertBox = styled(Alert)`
    margin: 24px auto 0px auto;
    max-width: 400px;
`

const ContentWrapper = styled.div`
    margin: 24px auto auto auto;
    max-width: 400px;
`

const Login: React.FC = () => {

    const { search } = useLocation()

    let loggedOut = false
    let error = null

    const queryParams = queryString.parse(search)

    const logoutParam = queryParams['logout']
    const errorParam = queryParams['error']

    if (logoutParam !== undefined) {
        loggedOut = true
    }

    if (typeof errorParam === 'string') {
        if (errorParam in errorMessages) {
            error = errorMessages[errorParam]
        } else {
            error = 'An error occurred while logging in. Please try again later.'
        }
    }

    return (
        <>
            {error && <AlertBox message={error} type="error" showIcon closable />}
            {loggedOut && <AlertBox message="You've successfully logged out of SackBot." type="info" showIcon closable />}
            <ContentWrapper>
                <Title>Welcome to SackBot!</Title>
                <Paragraph>Please log in via Discord to use the application.</Paragraph>
                <Button block type='primary' size='large' icon={<Icon component={DiscordSvg} style={{ fill: "#fff" }} />} href='/oauth2/authorization/discord'>Log in</Button>
            </ContentWrapper>
        </>
    )
}

export default Login