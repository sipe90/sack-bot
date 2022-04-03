import React from 'react'
import { Box } from '@mui/material'
import { SxProps, Theme } from '@mui/material/styles'
import { styled } from '@mui/system'

const Border = styled('div')({
    borderBottom: '2px solid lightgray',
    width: '100%'
})

const Content = styled('span')(({ theme }) => ({
    padding: theme.spacing(0.5, 2),
    fontWeight: 500,
    fontSize: 22,
    color: 'lightgray'
}))

const Divider: React.FC<{ sx?: SxProps<Theme> }> = ({ sx, children }) => (
    <Box display='flex' alignItems='center' my={2} sx={sx}>
        {children ?
            <>
                <Border />
                <Content>{children}</Content>
                <Border />
            </>
            : <Border />
        }
    </Box>
)


export default Divider