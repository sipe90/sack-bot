import React from 'react'
import Box from '@mui/material/Grid'
import Paper from '@mui/material/Paper'
import Grid from '@mui/material/Grid'
import Typography from '@mui/material/Typography'


import sackbotImg_alt from '@/public/img/Sackbot_V9001.png'


const NotFound: React.FC = () => (
    <Grid component='div' container sx={{ height: 1100 }}>
        <Grid item xs={false} sm={4} md={7} sx={(theme) => ({
            backgroundImage: `url(${sackbotImg_alt})`,
            backgroundRepeat: 'no-repeat',
            backgroundColor:
                theme.palette.mode === 'light' ? theme.palette.grey[50] : theme.palette.grey[900],
            backgroundSize: 'cover',
            backgroundPosition: 'top',
        })} />
        <Grid item xs={12} sm={8} md={5} component={Paper} elevation={6} square>
            <Box sx={{
                mx: 8,
                my: 8,
                display: 'flex',
                flexDirection: 'column',
                alignItems: 'center',
            }}>
                <Typography component='h1' variant='h1'>
                    404
                </Typography>
            </Box>
        </Grid>
    </Grid>
)

export default NotFound