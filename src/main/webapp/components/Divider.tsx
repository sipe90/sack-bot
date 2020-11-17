import React from 'react'
import { Box, createStyles, makeStyles } from '@material-ui/core'

const useStyles = makeStyles(theme => createStyles({
    container: {
        display: 'flex',
        alignItems: 'center',
        marginTop: theme.spacing(2),
        marginBottom: theme.spacing(2)
    },
    border: {
        borderBottom: '2px solid lightgray',
        width: '100%'
    },
    content: {
        paddingTop: theme.spacing(0.5),
        paddingBottom: theme.spacing(0.5),
        paddingRight: theme.spacing(2),
        paddingLeft: theme.spacing(2),
        fontWeight: 500,
        fontSize: 22,
        color: 'lightgray'
    }
}))

const Divider: React.FC<{ className?: string, style?: React.CSSProperties }> = ({ children, className, style }) => {
    const classes = useStyles()
    return (
        <Box display='flex' alignItems='center' my={2} className={className} style={style}>
            {children ?
                <>
                    <div className={classes.border} />
                    <span className={classes.content}>{children}</span>
                    <div className={classes.border} />
                </>
                : <div className={classes.border} />
            }
        </Box>
    )
}

export default Divider